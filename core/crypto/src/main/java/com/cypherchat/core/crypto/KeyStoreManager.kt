package com.cypherchat.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.cypherchat.core.common.CypherError
import com.cypherchat.core.common.SecureResult
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

private const val KEYSTORE_PROVIDER  = "AndroidKeyStore"
private const val KEY_ALIAS_DB       = "cypherchat_db_key"
private const val KEY_ALIAS_MSG      = "cypherchat_msg_key"

/**
 * Manages AES-256-GCM keys in the Android Hardware-backed Keystore.
 *
 * SECURITY notes:
 * - Keys are generated inside the secure element (hardware-backed on API 28+).
 * - Keys never leave the Keystore — only encrypt/decrypt operations are performed.
 * - No user authentication required here; app-level lock is handled separately.
 * - BLOCK_MODE_GCM provides authenticated encryption (integrity + confidentiality).
 */
object KeyStoreManager {

    /** Returns the database encryption key, generating it on first call. */
    fun getDatabaseKey(): SecureResult<SecretKey> = getOrCreate(KEY_ALIAS_DB)

    /** Returns the message encryption key, generating it on first call. */
    fun getMessageKey(): SecureResult<SecretKey> = getOrCreate(KEY_ALIAS_MSG)

    fun deleteAll() {
        try {
            val ks = keyStore()
            if (ks.containsAlias(KEY_ALIAS_DB))  ks.deleteEntry(KEY_ALIAS_DB)
            if (ks.containsAlias(KEY_ALIAS_MSG)) ks.deleteEntry(KEY_ALIAS_MSG)
        } catch (_: Exception) { /* Best-effort */ }
    }

    // ── Private ──────────────────────────────────────────────────────────────

    private fun getOrCreate(alias: String): SecureResult<SecretKey> = try {
        val ks = keyStore()
        if (!ks.containsAlias(alias)) generateKey(alias)
        val key = ks.getKey(alias, null) as? SecretKey
            ?: return SecureResult.Failure(CypherError.CryptoError("Key not found: $alias"))
        SecureResult.Success(key)
    } catch (e: Exception) {
        SecureResult.Failure(CypherError.CryptoError("KeyStore error for $alias", e))
    }

    private fun generateKey(alias: String) {
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            // Attempt hardware backing; falls back to software on older devices.
            .setIsStrongBoxBacked(false)    // set true if targeting API 28+ exclusively
            .build()

        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            .apply { init(spec) }
            .generateKey()
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        .also { it.load(null) }
}
