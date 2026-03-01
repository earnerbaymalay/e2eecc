package com.cypherchat.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single message in a conversation.
 *
 * SECURITY: [encryptedContent] stores the Double-Ratchet ciphertext.
 * The database is further protected by SQLCipher (AES-256).
 * Plaintext is NEVER persisted.
 */
@Entity(
    tableName = "messages",
    indices = [
        Index("conversation_id"),
        Index("timestamp")
    ]
)
data class MessageEntity(
    @PrimaryKey val id: String,                                     // UUID
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "sender_key_fingerprint") val senderKeyFingerprint: String,
    @ColumnInfo(name = "encrypted_content",
                typeAffinity = ColumnInfo.BLOB) val encryptedContent: ByteArray,
    @ColumnInfo(name = "timestamp") val timestamp: Long,            // epoch ms
    @ColumnInfo(name = "is_outgoing") val isOutgoing: Boolean,
    @ColumnInfo(name = "delivered") val delivered: Boolean = false,
    @ColumnInfo(name = "read") val read: Boolean = false,
    @ColumnInfo(name = "send_msg_num") val sendMsgNum: Int = 0,     // DR counter
    @ColumnInfo(name = "ratchet_key",
                typeAffinity = ColumnInfo.BLOB) val ratchetKey: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MessageEntity) return false
        return id == other.id &&
               conversationId == other.conversationId &&
               encryptedContent.contentEquals(other.encryptedContent)
    }

    override fun hashCode(): Int = id.hashCode()
}
