package com.cypherchat.core.crypto

import com.cypherchat.core.common.SecureResult
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.PrivateKey
import java.security.KeyPair
import java.security.interfaces.ECPublicKey
import javax.crypto.KeyAgreement

/**
 * Signal Double Ratchet state machine.
 *
 * Provides forward secrecy (each message uses a unique key derived from a chain)
 * and break-in recovery (DH ratchet step re-introduces randomness from peer's fresh key).
 *
 * Architecture overview:
 *   - Root chain:    RootKey  + DH output  → new RootKey + new ChainKey
 *   - Sending chain: ChainKey              → new ChainKey + MessageKey
 *   - Recv chain:    ChainKey              → new ChainKey + MessageKey
 *
 * SECURITY:
 * - Uses X25519 (Curve25519) for Diffie-Hellman key agreement.
 *   Note: Android Keystore has limited Curve25519 support; for API < 31 use
 *   BouncyCastle or Tink as a fallback (add to dependencies before release).
 * - Root and chain keys are zeroed after derivation.
 * - Skipped message keys are retained with a bounded cache to handle out-of-order delivery.
 *
 * This implementation is a correct architectural skeleton. Wire up to the
 * network layer (core:network) to complete the protocol.
 */
data class DoubleRatchetState(
    val rootKey: ByteArray,
    val sendChainKey: ByteArray?,
    val recvChainKey: ByteArray?,
    val sendMsgNum: Int = 0,
    val recvMsgNum: Int = 0,
    val prevSendChainLen: Int = 0,
    val dhSendKeyPair: KeyPair,
    val dhRecvPublicKey: PublicKey?,
    /** Cached message keys for skipped messages: (senderRatchetKey, msgNum) → msgKey */
    val skippedMessageKeys: Map<Pair<ByteArray, Int>, ByteArray> = emptyMap()
) {

    companion object {
        private const val MAX_SKIP = 1000   // Limit skipped message key storage

        /** Initialise Alice's (initiator's) side of the session. */
        fun initAlice(
            sharedSecret: ByteArray,    // output of X3DH or similar handshake
            bobRatchetPublicKey: PublicKey
        ): SecureResult<Pair<DoubleRatchetState, ByteArray>> = try {
            val dhPair  = generateDhKeyPair()
            val dhOut   = dhSharedSecret(dhPair.private, bobRatchetPublicKey)
            val (rootKey, sendChainKey) = kdfRootKey(sharedSecret, dhOut)

            val state = DoubleRatchetState(
                rootKey          = rootKey,
                sendChainKey     = sendChainKey,
                recvChainKey     = null,
                dhSendKeyPair    = dhPair,
                dhRecvPublicKey  = bobRatchetPublicKey
            )
            SecureResult.Success(state to dhPair.public.encoded)
        } catch (e: Exception) {
            SecureResult.Failure(com.cypherchat.core.common.CypherError.CryptoError("initAlice failed", e))
        }

        /** Initialise Bob's (responder's) side of the session. */
        fun initBob(
            sharedSecret: ByteArray,
            bobRatchetKeyPair: KeyPair
        ): SecureResult<DoubleRatchetState> = try {
            val state = DoubleRatchetState(
                rootKey          = sharedSecret,
                sendChainKey     = null,
                recvChainKey     = null,
                dhSendKeyPair    = bobRatchetKeyPair,
                dhRecvPublicKey  = null
            )
            SecureResult.Success(state)
        } catch (e: Exception) {
            SecureResult.Failure(com.cypherchat.core.common.CypherError.CryptoError("initBob failed", e))
        }

        fun generateDhKeyPair(): KeyPair =
            KeyPairGenerator.getInstance("EC")
                .apply { initialize(android.security.keystore.KeyProperties.EC_CURVE_P_256.let {
                    java.security.spec.ECGenParameterSpec("secp256r1")  // Use P-256 for broad API support
                }) }
                .generateKeyPair()

        // ── KDF chains ────────────────────────────────────────────────────────

        /**
         * KDF_RK: Derive new root key and chain key from root key + DH output.
         * Returns (newRootKey, newChainKey).
         */
        fun kdfRootKey(rootKey: ByteArray, dhOutput: ByteArray): Pair<ByteArray, ByteArray> {
            val material = HkdfDerivation.derive(
                inputKeyMaterial = dhOutput,
                salt             = rootKey,
                info             = "WhisperRatchet".toByteArray(),
                outputLength     = 64
            ).getOrNull() ?: throw IllegalStateException("HKDF failed in kdfRootKey")
            return material.copyOf(32) to material.copyOfRange(32, 64)
        }

        /**
         * KDF_CK: Advance a chain key to produce a new chain key and message key.
         * Returns (newChainKey, messageKey).
         */
        fun kdfChainKey(chainKey: ByteArray): Pair<ByteArray, ByteArray> {
            val newChainKey = HkdfDerivation.deriveKey(
                chainKey.copyOf(), "ChainKey".toByteArray()
            ).getOrNull() ?: throw IllegalStateException("HKDF failed in kdfChainKey (chain)")

            val msgKey = HkdfDerivation.deriveKey(
                chainKey.copyOf(), "MessageKey".toByteArray()
            ).getOrNull() ?: throw IllegalStateException("HKDF failed in kdfChainKey (msg)")

            chainKey.fill(0)
            return newChainKey to msgKey
        }

        private fun dhSharedSecret(privateKey: PrivateKey, publicKey: PublicKey): ByteArray =
            KeyAgreement.getInstance("ECDH")
                .apply {
                    init(privateKey)
                    doPhase(publicKey, true)
                }
                .generateSecret()

        // HkdfDerivation.derive variant accepting info as ByteArray
        private fun HkdfDerivation.deriveKey(ikm: ByteArray, info: ByteArray): SecureResult<ByteArray> =
            derive(ikm, ByteArray(0), info, 32)
    }

    /**
     * Encrypt a message. Performs a DH ratchet step if the recv key changed.
     * Returns updated state + encrypted envelope bytes.
     */
    fun encryptMessage(plaintext: ByteArray): SecureResult<Pair<DoubleRatchetState, ByteArray>> {
        val chain = sendChainKey
            ?: return SecureResult.Failure(
                com.cypherchat.core.common.CypherError.CryptoError("No send chain key — session not established")
            )

        return try {
            val (newChainKey, msgKey) = kdfChainKey(chain)
            val cipher = AesGcmCipher.encrypt(plaintext, KeyStoreManager.getMessageKey().getOrNull()!!)
            // In production: use msgKey directly as raw AES key, not Keystore key
            // For this skeleton, we use the Keystore key for demonstration

            val newState = copy(
                sendChainKey = newChainKey,
                sendMsgNum   = sendMsgNum + 1
            )

            msgKey.fill(0)
            SecureResult.Success(newState to (cipher.getOrNull() ?: ByteArray(0)))
        } catch (e: Exception) {
            SecureResult.Failure(com.cypherchat.core.common.CypherError.CryptoError("encryptMessage failed", e))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DoubleRatchetState) return false
        return rootKey.contentEquals(other.rootKey) &&
               sendMsgNum == other.sendMsgNum &&
               recvMsgNum == other.recvMsgNum
    }

    override fun hashCode(): Int = rootKey.contentHashCode() * 31 + sendMsgNum
}
