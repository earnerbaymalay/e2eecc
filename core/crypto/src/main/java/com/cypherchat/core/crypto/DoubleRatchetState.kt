package com.cypherchat.core.crypto

import com.cypherchat.core.common.CypherError
import com.cypherchat.core.common.SecureResult
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec
import javax.crypto.KeyAgreement

/**
 * Signal-compatible Double Ratchet implementation.
 * 
 * Provides:
 * - Forward secrecy (each message uses unique key)
 * - Break-in recovery (DH ratchet step)
 * - Out-of-order message handling (skipped message key cache)
 * 
 * Architecture:
 *   Root chain:  RootKey + DH output → (new RootKey, new ChainKey)
 *   Send chain:  ChainKey → (new ChainKey, MessageKey)
 *   Recv chain:  ChainKey → (new ChainKey, MessageKey)
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
    val skippedMessageKeys: Map<Pair<String, Int>, ByteArray> = emptyMap()
) {

    companion object {
        private const val MAX_SKIP = 1000
        private const val HKDF_INFO_ROOT = "WhisperRatchet"
        private const val HKDF_INFO_CHAIN = "ChainKey"
        private const val HKDF_INFO_MSG = "MessageKey"

        /** Initialise Alice's (initiator) side of the session. */
        fun initAlice(
            sharedSecret: ByteArray,
            bobRatchetPublicKey: PublicKey
        ): SecureResult<Pair<DoubleRatchetState, ByteArray>> = try {
            val dhPair = generateDhKeyPair()
            val dhOut = dhSharedSecret(dhPair.private, bobRatchetPublicKey)
            val (rootKey, sendChainKey) = kdfRootKey(sharedSecret, dhOut)

            val state = DoubleRatchetState(
                rootKey = rootKey,
                sendChainKey = sendChainKey,
                recvChainKey = null,
                dhSendKeyPair = dhPair,
                dhRecvPublicKey = bobRatchetPublicKey
            )
            SecureResult.Success(state to dhPair.public.encoded)
        } catch (e: Exception) {
            SecureResult.Failure(CypherError.CryptoError("initAlice failed", e))
        }

        /** Initialise Bob's (responder) side of the session. */
        fun initBob(
            sharedSecret: ByteArray,
            bobRatchetKeyPair: KeyPair,
            aliceRatchetPublicKey: PublicKey
        ): SecureResult<DoubleRatchetState> = try {
            val dhOut = dhSharedSecret(bobRatchetKeyPair.private, aliceRatchetPublicKey)
            val (rootKey, recvChainKey) = kdfRootKey(sharedSecret, dhOut)

            val state = DoubleRatchetState(
                rootKey = rootKey,
                sendChainKey = null,
                recvChainKey = recvChainKey,
                dhSendKeyPair = bobRatchetKeyPair,
                dhRecvPublicKey = aliceRatchetPublicKey
            )
            SecureResult.Success(state)
        } catch (e: Exception) {
            SecureResult.Failure(CypherError.CryptoError("initBob failed", e))
        }

        /** Create initial state from X3DH handshake result. */
        fun fromX3DH(
            sharedSecret: ByteArray,
            myKeyPair: KeyPair,
            theirRatchetKey: PublicKey,
            isInitiator: Boolean
        ): SecureResult<DoubleRatchetState> {
            return if (isInitiator) {
                initAlice(sharedSecret, theirRatchetKey).map { (state, _) -> state }
            } else {
                initBob(sharedSecret, myKeyPair, theirRatchetKey)
            }
        }

        fun generateDhKeyPair(): KeyPair =
            KeyPairGenerator.getInstance("EC").apply {
                initialize(ECGenParameterSpec("secp256r1"))
            }.generateKeyPair()

        /**
         * KDF_RK: Derive new root key and chain key from root key + DH output.
         */
        fun kdfRootKey(rootKey: ByteArray, dhOutput: ByteArray): Pair<ByteArray, ByteArray> {
            val material = HkdfDerivation.derive(
                inputKeyMaterial = dhOutput,
                salt = rootKey,
                info = HKDF_INFO_ROOT.toByteArray(),
                outputLength = 64
            ).getOrNull() ?: throw IllegalStateException("HKDF failed in kdfRootKey")
            return material.copyOf(32) to material.copyOfRange(32, 64)
        }

        /**
         * KDF_CK: Advance chain key to produce new chain key + message key.
         */
        fun kdfChainKey(chainKey: ByteArray): Pair<ByteArray, ByteArray> {
            val newChainKey = HkdfDerivation.derive(
                inputKeyMaterial = chainKey,
                salt = ByteArray(0),
                info = HKDF_INFO_CHAIN.toByteArray(),
                outputLength = 32
            ).getOrNull() ?: throw IllegalStateException("HKDF chain failed")

            val msgKey = HkdfDerivation.derive(
                inputKeyMaterial = chainKey,
                salt = ByteArray(0),
                info = HKDF_INFO_MSG.toByteArray(),
                outputLength = 32
            ).getOrNull() ?: throw IllegalStateException("HKDF msg failed")

            return newChainKey to msgKey
        }

        private fun dhSharedSecret(privateKey: PrivateKey, publicKey: PublicKey): ByteArray =
            KeyAgreement.getInstance("ECDH").apply {
                init(privateKey)
                doPhase(publicKey, true)
            }.generateSecret()

        private fun ByteArray.toHexKey(): String = joinToString("") { "%02x".format(it) }
    }

    /**
     * Encrypt a plaintext message using the Double Ratchet.
     * Returns updated state + encrypted envelope.
     */
    fun encryptMessage(plaintext: ByteArray): SecureResult<Pair<DoubleRatchetState, ByteArray>> {
        val chain = sendChainKey
            ?: return SecureResult.Failure(CypherError.CryptoError("No send chain — session not established"))

        return try {
            val (newChainKey, msgKey) = kdfChainKey(chain)
            
            // Use the ratchet-derived message key directly (not Keystore key)
            val cipherResult = AesGcmCipher.encrypt(plaintext, msgKey)
            val ciphertext = cipherResult.getOrThrow()

            val newState = copy(
                sendChainKey = newChainKey,
                sendMsgNum = sendMsgNum + 1
            )

            msgKey.fill(0)
            SecureResult.Success(newState to ciphertext)
        } catch (e: Exception) {
            SecureResult.Failure(CypherError.CryptoError("encrypt failed", e))
        }
    }

    /**
     * Decrypt a ciphertext message using the Double Ratchet.
     * Handles out-of-order messages via skipped key cache.
     * Returns updated state + decrypted plaintext.
     */
    fun decryptMessage(
        ciphertext: ByteArray,
        senderRatchetKey: PublicKey,
        msgNum: Int
    ): SecureResult<Pair<DoubleRatchetState, ByteArray>> {
        return try {
            // Check if we need a DH ratchet step (new sender ratchet key)
            var state = this
            var needsRatchet = false
            val recvKey = state.dhRecvPublicKey

            if (recvKey == null ||
                !recvKey.encoded.contentEquals(senderRatchetKey.encoded)) {
                needsRatchet = true
            }

            // Perform DH ratchet step if sender changed their ratchet key
            if (needsRatchet && state.sendChainKey != null) {
                state = state.dhRatchetStep(senderRatchetKey)
            }

            // Handle out-of-order messages
            val skipKey = senderRatchetKey.encoded.toHexKey() to msgNum
            if (msgNum < state.recvMsgNum) {
                val cachedKey = state.skippedMessageKeys[skipKey]
                if (cachedKey == null) {
                    return SecureResult.Failure(
                        CypherError.CryptoError("No key for skipped message #$msgNum")
                    )
                }
                val plaintext = AesGcmCipher.decrypt(ciphertext, cachedKey).getOrThrow()
                cachedKey.fill(0)
                val newSkipped = state.skippedMessageKeys - skipKey
                return SecureResult.Success(
                    state.copy(skippedMessageKeys = newSkipped) to plaintext
                )
            }

            // Skip forward for any missing messages (cache their keys)
            val newSkipped = state.skippedMessageKeys.toMutableMap()
            var currentRecvChain = state.recvChainKey
            var currentRecvNum = state.recvMsgNum

            while (currentRecvNum < msgNum && currentRecvChain != null) {
                if (newSkipped.size >= MAX_SKIP) {
                    return SecureResult.Failure(CypherError.CryptoError("Skipped message cache full"))
                }
                val (nextChain, msgKey) = kdfChainKey(currentRecvChain)
                newSkipped[senderRatchetKey.encoded.toHexKey() to currentRecvNum] = msgKey
                currentRecvChain = nextChain
                currentRecvNum++
            }

            // Derive the message key for this message number
            if (currentRecvChain == null) {
                return SecureResult.Failure(CypherError.CryptoError("No receive chain"))
            }

            val (newRecvChain, msgKey) = kdfChainKey(currentRecvChain)

            // Decrypt
            val plaintext = AesGcmCipher.decrypt(ciphertext, msgKey).getOrThrow()
            msgKey.fill(0)

            val newState = state.copy(
                recvChainKey = newRecvChain,
                recvMsgNum = msgNum + 1,
                dhRecvPublicKey = senderRatchetKey,
                skippedMessageKeys = newSkipped.toMap()
            )

            SecureResult.Success(newState to plaintext)
        } catch (e: Exception) {
            SecureResult.Failure(CypherError.CryptoError("decrypt failed", e))
        }
    }

    /**
     * Perform a DH ratchet step when the remote party sends a new ratchet key.
     * This re-establishes forward secrecy after the remote's key change.
     */
    private fun dhRatchetStep(remoteRatchetKey: PublicKey): DoubleRatchetState {
        val dhOut = dhSharedSecret(dhSendKeyPair.private, remoteRatchetKey)
        val (newRootKey, newRecvChainKey) = kdfRootKey(rootKey, dhOut)

        val newDhSendPair = generateDhKeyPair()
        val newDhOut = dhSharedSecret(newDhSendPair.private, remoteRatchetKey)
        val (_, newSendChainKey) = kdfRootKey(newRootKey, newDhOut)

        return copy(
            rootKey = newRootKey,
            sendChainKey = newSendChainKey,
            recvChainKey = newRecvChainKey,
            prevSendChainLen = sendMsgNum,
            dhSendKeyPair = newDhSendPair,
            dhRecvPublicKey = remoteRatchetKey,
            sendMsgNum = 0
        )
    }

    /** Get the current send ratchet public key (to include in message headers). */
    fun getSendRatchetPublicKey(): PublicKey = dhSendKeyPair.public

    /** Get the current send ratchet public key as bytes for transmission. */
    fun getSendRatchetKeyBytes(): ByteArray = dhSendKeyPair.public.encoded

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DoubleRatchetState) return false
        return rootKey.contentEquals(other.rootKey) &&
            sendMsgNum == other.sendMsgNum &&
            recvMsgNum == other.recvMsgNum
    }

    override fun hashCode(): Int = rootKey.contentHashCode() * 31 + sendMsgNum
}

