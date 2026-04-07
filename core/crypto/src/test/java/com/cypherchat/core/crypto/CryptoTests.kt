package com.cypherchat.core.crypto

import com.cypherchat.core.common.SecureResult
import org.junit.Test
import org.junit.Assert.*
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.Cipher

/**
 * JVM unit tests for AesGcmCipher and HkdfDerivation.
 * These test the pure-Java crypto components (no Android Keystore dependency).
 */
class AesGcmCipherTest {

    private fun generateTestKey(): javax.crypto.SecretKey {
        return KeyGenerator.getInstance("AES").apply {
            init(256)
        }.generateKey()
    }

    @Test
    fun `encrypt and decrypt round-trip`() {
        val key = generateTestKey()
        val plaintext = "Hello, Cypherchat!".toByteArray(Charsets.UTF_8)

        val encryptResult = AesGcmCipher.encrypt(plaintext, key)
        assertTrue(encryptResult is SecureResult.Success)

        val ciphertext = (encryptResult as SecureResult.Success).value

        val decryptResult = AesGcmCipher.decrypt(ciphertext, key)
        assertTrue(decryptResult is SecureResult.Success)

        val recovered = (decryptResult as SecureResult.Success).value
        assertArrayEquals(plaintext, recovered)
    }

    @Test
    fun `encrypted output is different from plaintext`() {
        val key = generateTestKey()
        val plaintext = "Secret message".toByteArray(Charsets.UTF_8)

        val result = AesGcmCipher.encrypt(plaintext, key)
        val ciphertext = (result as SecureResult.Success).value

        assertFalse(plaintext.contentEquals(ciphertext))
    }

    @Test
    fun `each encryption produces different ciphertext (random IV)`() {
        val key = generateTestKey()
        val plaintext = "Same message twice".toByteArray(Charsets.UTF_8)

        val result1 = AesGcmCipher.encrypt(plaintext.copyOf(), key)
        val result2 = AesGcmCipher.encrypt(plaintext.copyOf(), key)

        val ct1 = (result1 as SecureResult.Success).value
        val ct2 = (result2 as SecureResult.Success).value

        assertFalse(ct1.contentEquals(ct2))
    }

    @Test
    fun `envelope format has correct structure`() {
        val key = generateTestKey()
        val plaintext = "Test".toByteArray(Charsets.UTF_8)

        val result = AesGcmCipher.encrypt(plaintext, key)
        val ciphertext = (result as SecureResult.Success).value

        // [VERSION(1)] [IV(12)] [CT+TAG(at least 16)]
        assertTrue(ciphertext.size >= 1 + 12 + 16)
        assertEquals(0x01.toByte(), ciphertext[0])
    }

    @Test
    fun `decrypt rejects tampered ciphertext`() {
        val key = generateTestKey()
        val plaintext = "Don't tamper with this".toByteArray(Charsets.UTF_8)

        val encryptResult = AesGcmCipher.encrypt(plaintext, key)
        val ciphertext = (encryptResult as SecureResult.Success).value

        // Tamper with the ciphertext
        ciphertext[ciphertext.size - 1] = (ciphertext[ciphertext.size - 1].toInt() xor 0xFF).toByte()

        val decryptResult = AesGcmCipher.decrypt(ciphertext, key)
        assertTrue(decryptResult is SecureResult.Failure)
    }

    @Test
    fun `decrypt rejects too-short envelope`() {
        val key = generateTestKey()
        val shortEnvelope = byteArrayOf(0x01, 0x00, 0x01, 0x02) // way too short

        val result = AesGcmCipher.decrypt(shortEnvelope, key)
        assertTrue(result is SecureResult.Failure)
    }

    @Test
    fun `decrypt rejects unknown version`() {
        val key = generateTestKey()
        val plaintext = "Test".toByteArray(Charsets.UTF_8)

        val result = AesGcmCipher.encrypt(plaintext, key)
        val ciphertext = (result as SecureResult.Success).value

        // Change version byte
        ciphertext[0] = 0x99.toByte()

        val decryptResult = AesGcmCipher.decrypt(ciphertext, key)
        assertTrue(decryptResult is SecureResult.Failure)
    }

    @Test
    fun `empty plaintext encrypts and decrypts`() {
        val key = generateTestKey()
        val plaintext = ByteArray(0)

        val encryptResult = AesGcmCipher.encrypt(plaintext, key)
        assertTrue(encryptResult is SecureResult.Success)

        val ciphertext = (encryptResult as SecureResult.Success).value

        val decryptResult = AesGcmCipher.decrypt(ciphertext, key)
        assertTrue(decryptResult is SecureResult.Success)

        val recovered = (decryptResult as SecureResult.Success).value
        assertArrayEquals(plaintext, recovered)
    }

    @Test
    fun `large message encrypts and decrypts`() {
        val key = generateTestKey()
        val plaintext = ByteArray(10000) { (it % 256).toByte() }

        val encryptResult = AesGcmCipher.encrypt(plaintext, key)
        val ciphertext = (encryptResult as SecureResult.Success).value

        val decryptResult = AesGcmCipher.decrypt(ciphertext, key)
        val recovered = (decryptResult as SecureResult.Success).value

        assertArrayEquals(plaintext, recovered)
    }

    @Test
    fun `AAD binding - tampering AAD causes decryption failure`() {
        val key = generateTestKey()
        val plaintext = "AAD bound message".toByteArray(Charsets.UTF_8)
        val originalAad = "original_aad".toByteArray()

        val encryptResult = AesGcmCipher.encrypt(plaintext, key, originalAad)
        val ciphertext = (encryptResult as SecureResult.Success).value

        // Try to decrypt with different AAD
        val wrongAad = "wrong_aad".toByteArray()
        val decryptResult = AesGcmCipher.decrypt(ciphertext, key, wrongAad)
        assertTrue(decryptResult is SecureResult.Failure)
    }
}

class HkdfDerivationTest {

    @Test
    fun `derive produces correct length output`() {
        val ikm = ByteArray(32) { 0x0B }
        val salt = ByteArray(32) { 0x00 }
        val info = "test".toByteArray()

        val result = HkdfDerivation.derive(ikm, salt, info, 32)
        assertTrue(result is SecureResult.Success)
        assertEquals(32, (result as SecureResult.Success).value.size)
    }

    @Test
    fun `same input produces same output (deterministic)`() {
        val ikm = "input-key-material".toByteArray(Charsets.UTF_8)
        val salt = "salt-value".toByteArray(Charsets.UTF_8)
        val info = "info".toByteArray(Charsets.UTF_8)

        val result1 = HkdfDerivation.derive(ikm, salt, info, 32)
        val result2 = HkdfDerivation.derive(ikm, salt, info, 32)

        val out1 = (result1 as SecureResult.Success).value
        val out2 = (result2 as SecureResult.Success).value

        assertArrayEquals(out1, out2)
    }

    @Test
    fun `different salt produces different output`() {
        val ikm = "input-key-material".toByteArray(Charsets.UTF_8)
        val info = "info".toByteArray(Charsets.UTF_8)

        val result1 = HkdfDerivation.derive(ikm, "salt1".toByteArray(), info, 32)
        val result2 = HkdfDerivation.derive(ikm, "salt2".toByteArray(), info, 32)

        val out1 = (result1 as SecureResult.Success).value
        val out2 = (result2 as SecureResult.Success).value

        assertFalse(out1.contentEquals(out2))
    }

    @Test
    fun `different info produces different output`() {
        val ikm = "input-key-material".toByteArray(Charsets.UTF_8)
        val salt = "salt".toByteArray(Charsets.UTF_8)

        val result1 = HkdfDerivation.derive(ikm, salt, "info1".toByteArray(), 32)
        val result2 = HkdfDerivation.derive(ikm, salt, "info2".toByteArray(), 32)

        val out1 = (result1 as SecureResult.Success).value
        val out2 = (result2 as SecureResult.Success).value

        assertFalse(out1.contentEquals(out2))
    }

    @Test
    fun `derives longer keys correctly`() {
        val ikm = ByteArray(16) { it.toByte() }
        val salt = ByteArray(16) { it.toByte() }
        val info = "test".toByteArray()

        val result64 = HkdfDerivation.derive(ikm, salt, info, 64)
        assertTrue(result64 is SecureResult.Success)
        assertEquals(64, (result64 as SecureResult.Success).value.size)
    }
}
