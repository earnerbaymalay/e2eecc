package com.cypherchat.core.crypto

import com.cypherchat.core.common.CypherError
import com.cypherchat.core.common.SecureResult
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val ALGORITHM   = "AES/GCM/NoPadding"
private const val IV_SIZE     = 12     // 96 bits — NIST recommended for GCM
private const val TAG_SIZE    = 128    // 128-bit authentication tag
private const val VERSION     = 0x01.toByte()   // envelope version byte

/**
 * Authenticated encryption using AES-256-GCM.
 *
 * Ciphertext envelope layout:
 *   [VERSION(1)] [IV(12)] [CIPHERTEXT+TAG(n)]
 *
 * SECURITY:
 * - IV is random 96-bit per operation (never reuse an IV with the same key).
 * - Tag is 128 bits (maximum GCM authentication strength).
 * - Additional authenticated data (AAD) binds the envelope to its context.
 */
object AesGcmCipher {

    private val rng = SecureRandom()

    /**
     * Encrypts [plaintext] with [key].
     * @param aad Additional authenticated data (e.g. message ID, sender key fingerprint).
     *            Not encrypted, but authenticated — any change will cause decryption to fail.
     */
    fun encrypt(
        plaintext: ByteArray,
        key: SecretKey,
        aad: ByteArray = ByteArray(0)
    ): SecureResult<ByteArray> = try {
        val iv = ByteArray(IV_SIZE).also { rng.nextBytes(it) }

        val cipher = Cipher.getInstance(ALGORITHM).apply {
            init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_SIZE, iv))
            if (aad.isNotEmpty()) updateAAD(aad)
        }

        val ct = cipher.doFinal(plaintext)

        // Wipe plaintext from local scope on a best-effort basis
        // (JVM GC does not guarantee immediate collection, but this reduces window)
        plaintext.fill(0)

        val envelope = ByteArray(1 + IV_SIZE + ct.size)
        envelope[0] = VERSION
        System.arraycopy(iv, 0, envelope, 1, IV_SIZE)
        System.arraycopy(ct, 0, envelope, 1 + IV_SIZE, ct.size)

        SecureResult.Success(envelope)
    } catch (e: Exception) {
        SecureResult.Failure(CypherError.CryptoError("Encryption failed", e))
    }

    /**
     * Decrypts [envelope] (output of [encrypt]) with [key].
     */
    fun decrypt(
        envelope: ByteArray,
        key: SecretKey,
        aad: ByteArray = ByteArray(0)
    ): SecureResult<ByteArray> = try {
        if (envelope.size < 1 + IV_SIZE + 16) {
            return SecureResult.Failure(CypherError.CryptoError("Envelope too short"))
        }
        if (envelope[0] != VERSION) {
            return SecureResult.Failure(CypherError.CryptoError("Unknown envelope version ${envelope[0]}"))
        }

        val iv = envelope.copyOfRange(1, 1 + IV_SIZE)
        val ct = envelope.copyOfRange(1 + IV_SIZE, envelope.size)

        val plaintext = Cipher.getInstance(ALGORITHM).run {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_SIZE, iv))
            if (aad.isNotEmpty()) updateAAD(aad)
            doFinal(ct)
        }

        iv.fill(0)

        SecureResult.Success(plaintext)
    } catch (e: Exception) {
        SecureResult.Failure(CypherError.CryptoError("Decryption failed", e))
    }
}
