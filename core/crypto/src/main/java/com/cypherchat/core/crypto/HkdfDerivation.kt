package com.cypherchat.core.crypto

import com.cypherchat.core.common.CypherError
import com.cypherchat.core.common.SecureResult
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val HMAC_ALGO = "HmacSHA256"
private const val HASH_LEN  = 32    // SHA-256 output length in bytes

/**
 * HKDF (HMAC-based Key Derivation Function) per RFC 5869.
 *
 * Used to derive per-session and per-message keys from Diffie-Hellman outputs.
 *
 * SECURITY:
 * - Extract phase produces a uniformly-random pseudorandom key (PRK).
 * - Expand phase derives arbitrary-length output from PRK + context.
 * - Info parameter binds the derived key to its intended purpose.
 */
object HkdfDerivation {

    /**
     * Derives [outputLength] bytes from [inputKeyMaterial].
     *
     * @param inputKeyMaterial Raw key material (e.g. DH output).
     * @param salt             Optional salt. Use empty array to default to zero-filled hash-len.
     * @param info             Context string binding the key to its purpose.
     * @param outputLength     Desired output key length in bytes (max 255 * 32 = 8160).
     */
    fun derive(
        inputKeyMaterial: ByteArray,
        salt: ByteArray = ByteArray(0),
        info: ByteArray,
        outputLength: Int
    ): SecureResult<ByteArray> {
        require(outputLength > 0 && outputLength <= 255 * HASH_LEN) {
            "Output length must be 1..${255 * HASH_LEN}"
        }
        return try {
            val prk = extract(salt, inputKeyMaterial)
            SecureResult.Success(expand(prk, info, outputLength))
        } catch (e: Exception) {
            SecureResult.Failure(CypherError.CryptoError("HKDF derivation failed", e))
        }
    }

    /** Convenience: derive a single 32-byte key with a String context label. */
    fun deriveKey(
        inputKeyMaterial: ByteArray,
        context: String,
        salt: ByteArray = ByteArray(0)
    ): SecureResult<ByteArray> = derive(inputKeyMaterial, salt, context.toByteArray(), 32)

    // ── RFC 5869 internals ────────────────────────────────────────────────────

    private fun extract(salt: ByteArray, ikm: ByteArray): ByteArray {
        val actualSalt = if (salt.isEmpty()) ByteArray(HASH_LEN) else salt
        return hmacSha256(actualSalt, ikm)
    }

    private fun expand(prk: ByteArray, info: ByteArray, length: Int): ByteArray {
        val n = Math.ceil(length.toDouble() / HASH_LEN).toInt()
        val okm = ByteArray(n * HASH_LEN)
        var t = ByteArray(0)
        for (i in 1..n) {
            t = hmacSha256(prk, t + info + byteArrayOf(i.toByte()))
            System.arraycopy(t, 0, okm, (i - 1) * HASH_LEN, HASH_LEN)
        }
        return okm.copyOf(length)
    }

    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray =
        Mac.getInstance(HMAC_ALGO)
            .apply { init(SecretKeySpec(key, HMAC_ALGO)) }
            .doFinal(data)
}
