package com.cypherchat.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cypherchat.core.common.Logger
import com.cypherchat.core.database.dao.ContactDao
import com.cypherchat.core.database.dao.MessageDao
import com.cypherchat.core.database.entity.ContactEntity
import com.cypherchat.core.database.entity.MessageEntity
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom

private const val TAG = "AppDatabase"
private const val DB_NAME = "cypherchat.db"

/**
 * SQLCipher-encrypted Room database.
 *
 * A random 32-byte passphrase is generated on first launch and persisted in SharedPreferences.
 * The database file is encrypted at rest with AES-256-CBC (SQLCipher default).
 *
 * SECURITY: The database cannot be read without the SharedPreferences file.
 * For hardware-backed Keystore message encryption, see KeyStoreManager.
 */
@Database(
    entities = [MessageEntity::class, ContactEntity::class],
    version = 1,
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
            // Get or generate a stable 32-byte passphrase.
            // Stored in SharedPreferences — DB is still SQLCipher-encrypted at rest.
            val passphrase = getOrCreatePassphrase(context)

            return try {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .openHelperFactory(SupportFactory(passphrase))
                    .fallbackToDestructiveMigration()
                    .build()
            } finally {
                passphrase.fill(0)
                Logger.d(TAG, "Database opened, passphrase zeroed")
            }
        }

        /**
         * Retrieves a persisted passphrase or generates a new random one.
         * Generated once per install, stable across app restarts.
         */
        private fun getOrCreatePassphrase(context: Context): ByteArray {
            val prefs = context.getSharedPreferences("cypherchat_db", Context.MODE_PRIVATE)
            val existing = prefs.getString("db_passphrase", null)
            if (existing != null) {
                return android.util.Base64.decode(existing, android.util.Base64.DEFAULT)
            }

            // Generate new random 32-byte passphrase
            val passphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }
            val encoded = android.util.Base64.encodeToString(passphrase, android.util.Base64.DEFAULT)
            prefs.edit().putString("db_passphrase", encoded).apply()
            Logger.d(TAG, "Generated new database passphrase")
            return passphrase
        }
    }
}
