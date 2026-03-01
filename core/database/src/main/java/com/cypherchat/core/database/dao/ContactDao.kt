package com.cypherchat.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cypherchat.core.database.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: ContactEntity)

    @Update
    suspend fun update(contact: ContactEntity)

    @Query("SELECT * FROM contacts ORDER BY display_name ASC")
    fun observeAll(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getById(id: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE conversation_id = :conversationId")
    suspend fun getByConversationId(conversationId: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE public_key_fingerprint = :fingerprint")
    suspend fun getByFingerprint(fingerprint: String): ContactEntity?

    @Query("UPDATE contacts SET verified = 1 WHERE id = :id")
    suspend fun markVerified(id: String)

    @Query("UPDATE contacts SET last_seen = :timestamp WHERE id = :id")
    suspend fun updateLastSeen(id: String, timestamp: Long)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteById(id: String)
}
