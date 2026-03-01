package com.cypherchat.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A contact (peer) with whom the user has a conversation.
 *
 * Identity is established by [publicKeyFingerprint] — no persistent server-side identity.
 * The [displayName] is local-only and never transmitted.
 */
@Entity(
    tableName = "contacts",
    indices = [Index("public_key_fingerprint", unique = true)]
)
data class ContactEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "public_key_fingerprint") val publicKeyFingerprint: String,
    @ColumnInfo(name = "public_key_bytes",
                typeAffinity = ColumnInfo.BLOB) val publicKeyBytes: ByteArray,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "last_seen") val lastSeen: Long = 0L,
    @ColumnInfo(name = "verified") val verified: Boolean = false,
    @ColumnInfo(name = "simplex_address") val simplexAddress: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContactEntity) return false
        return id == other.id && publicKeyFingerprint == other.publicKeyFingerprint
    }

    override fun hashCode(): Int = id.hashCode()
}
