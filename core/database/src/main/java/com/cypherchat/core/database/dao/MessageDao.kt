package com.cypherchat.core.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cypherchat.core.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

data class LastMessagePreview(
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "previewText") val previewText: String
)

data class UnreadCount(
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "count") val count: Long
)

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Update
    suspend fun update(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp ASC")
    fun observeMessages(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(conversationId: String, limit: Int = 50): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getById(id: String): MessageEntity?

    @Query("UPDATE messages SET delivered = 1 WHERE id = :id")
    suspend fun markDelivered(id: String)

    @Query("UPDATE messages SET read = 1 WHERE conversation_id = :conversationId")
    suspend fun markConversationRead(conversationId: String)

    @Query("SELECT COUNT(*) FROM messages WHERE conversation_id = :conversationId AND read = 0 AND is_outgoing = 0")
    fun unreadCount(conversationId: String): Flow<Int>

    @Query(
        """
        SELECT conversation_id, COUNT(*) as count
        FROM messages
        WHERE read = 0 AND is_outgoing = 0
        GROUP BY conversation_id
        """
    )
    fun observeAllUnreadCounts(): Flow<List<UnreadCount>>

    @Query("DELETE FROM messages WHERE conversation_id = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query(
        """
        SELECT conversation_id,
               CASE
                   WHEN is_outgoing = 1 THEN '✓ ' || substr(encrypted_content, 1, 50)
                   ELSE substr(encrypted_content, 1, 50)
               END as previewText
        FROM messages m1
        WHERE timestamp = (
            SELECT MAX(timestamp) FROM messages m2
            WHERE m2.conversation_id = m1.conversation_id
        )
        ORDER BY timestamp DESC
        """
    )
    fun observeAllLastMessages(): Flow<List<LastMessagePreview>>
}
