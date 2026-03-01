package com.cypherchat.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cypherchat.core.common.Logger
import com.cypherchat.core.crypto.KeyStoreManager
import com.cypherchat.core.database.dao.ContactDao
import com.cypherchat.core.database.dao.MessageDao
import com.cypherchat.core.database.entity.ContactEntity
import com.cypherchat.core.database.entity.MessageEntity
import net.sqlcipher.database.SupportFactory

private const val TAG = "AppDatabase"
private const val DB_NAME = "cypherchat.db"

/**
 * SQLCipher-encrypted Room database.
 *
 * The encryption key is generated and stored in the Android Keystore (hardware-backed
 * where available). The key is used to derive a 32-byte SQLCipher passphrase; the
 * passphrase is zeroed from memory after opening the database.
 *
 * SECURITY: The database file is encrypted at rest with AES-256-CBC (SQLCipher default).
 * Combined with the Android Keystore key management, the database cannot be read
 * without the device Keystore — i.e. data is tied to this device and user.
 */
@Database(
    entities  = [MessageEntity::class, ContactEntity::class],
    version   = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao

    companion object {

        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context).also { INSTANCE = it }
            }
        }

        private fun build(context: Context): AppDatabase {
            // Retrieve the Keystore-managed AES key to derive the SQLCipher passphrase.
            // We use the raw key bytes as the passphrase; SQLCipher derives its internal
            // key from this using PBKDF2 (iterations configured in SQLCipher).
            val keystoreKey = KeyStoreManager.getDatabaseKey()
                .getOrNull()
                ?: error("Failed to obtain database key from Keystore")

            // Derive 32-byte passphrase from the Keystore key's encoded form.
            // Note: SecretKey.encoded returns null for Keystore-bound keys on many devices.
            // For Keystore-bound keys, we use the key to encrypt a fixed nonce and use
            // that as the passphrase — ensuring the passphrase is device-bound.
            val passphrase = derivePassphraseFromKeystoreKey(context, keystoreKey)

            return try {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                    .openHelperFactory(SupportFactory(passphrase))
                    .fallbackToDestructiveMigration()   // Replace with proper migrations before release
                    .build()
            } finally {
                passphrase.fill(0)  // Zero passphrase immediately after use
                Logger.d(TAG, "Database opened, passphrase zeroed")
            }
        }

        /**
         * Derives a stable 32-byte passphrase for this device by encrypting a fixed
         * known value with the Keystore key. The ciphertext (first 32 bytes) is
         * deterministic per-device but cannot be reproduced without the Keystore key.
         *
         * Note: For devices where SecretKey.encoded is available (software Keystore),
         * the raw key bytes are used directly for better performance.
         */
        private fun derivePassphraseFromKeystoreKey(
            context: Context,
            key: javax.crypto.SecretKey
        ): ByteArray {
            val raw = key.encoded
            if (raw != null && raw.size >= 32) {
                return raw.copyOf(32)
            }

            // Hardware-bound key: use a stable device identifier as input
            val deviceId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: "cypherchat-default-db-seed"

            val encrypted = com.cypherchat.core.crypto.AesGcmCipher
                .encrypt(deviceId.toByteArray(), key, "db_passphrase_derivation".toByteArray())
                .getOrNull()
                ?: error("Failed to derive passphrase")

            // Use first 32 bytes of the encrypted output as the passphrase
            return encrypted.copyOf(32)
        }
    }
}
