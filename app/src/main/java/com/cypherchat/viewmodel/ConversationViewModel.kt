package com.cypherchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cypherchat.core.common.DispatcherProvider
import com.cypherchat.core.common.Logger
import com.cypherchat.core.common.SecureResult
import com.cypherchat.core.crypto.AesGcmCipher
import com.cypherchat.core.crypto.KeyStoreManager
import com.cypherchat.core.database.dao.ContactDao
import com.cypherchat.core.database.dao.MessageDao
import com.cypherchat.core.database.entity.ContactEntity
import com.cypherchat.core.database.entity.MessageEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "ConversationViewModel"

/**
 * UI-level message — decrypted and safe to display.
 */
data class UiMessage(
    val id: String,
    val text: String,
    val isOutgoing: Boolean,
    val timestamp: String,
    val delivered: Boolean = false,
    val verified: Boolean = false
)

data class ConversationUiState(
    val messages: List<UiMessage> = emptyList(),
    val contactName: String = "Unknown",
    val verified: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

class ConversationViewModel(
    private val conversationId: String,
    private val messageDao: MessageDao,
    private val contactDao: ContactDao,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    init {
        observeMessages()
        loadContactInfo()
    }

    private fun observeMessages() {
        viewModelScope.launch(dispatchers.io) {
            messageDao.observeMessages(conversationId)
                .map { entities ->
                    entities.map { decryptMessage(it) }
                }
                .collect { messages ->
                    _uiState.update { it.copy(messages = messages, isLoading = false) }
                }
        }
    }

    private fun loadContactInfo() {
        viewModelScope.launch(dispatchers.io) {
            val contact = contactDao.getByConversationId(conversationId)
            if (contact != null) {
                _uiState.update {
                    it.copy(
                        contactName = contact.displayName,
                        verified = contact.verified
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        contactName = "New Conversation",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Sends a message: encrypts, stores ciphertext, queues for network.
     */
    fun sendMessage(plaintext: String) {
        if (plaintext.isBlank()) return

        viewModelScope.launch(dispatchers.io) {
            val messageId = UUID.randomUUID().toString()
            val aad = "$messageId:$conversationId".toByteArray()

            // Encrypt the message
            val keyResult = KeyStoreManager.getMessageKey()
            val cipherResult = when (keyResult) {
                is SecureResult.Success -> AesGcmCipher.encrypt(
                    plaintext.toByteArray(Charsets.UTF_8),
                    keyResult.value,
                    aad
                )
                is SecureResult.Failure -> {
                    Logger.e(TAG, "Failed to get message key: ${keyResult.error}")
                    _uiState.update { it.copy(error = "Encryption failed") }
                    return@launch
                }
            }

            val ciphertext = when (cipherResult) {
                is SecureResult.Success -> cipherResult.value
                is SecureResult.Failure -> {
                    Logger.e(TAG, "Encryption failed: ${cipherResult.error}")
                    _uiState.update { it.copy(error = "Failed to encrypt message") }
                    return@launch
                }
            }

            // Store encrypted message
            val entity = MessageEntity(
                id = messageId,
                conversationId = conversationId,
                senderKeyFingerprint = "local_key",
                encryptedContent = ciphertext,
                timestamp = System.currentTimeMillis(),
                isOutgoing = true,
                delivered = false
            )
            messageDao.insert(entity)
            Logger.d(TAG, "Message encrypted and stored: $messageId")

            // TODO: Send via SimpleX transport
            // transport.sendMessage(connection, ciphertext)
        }
    }

    fun markRead() {
        viewModelScope.launch(dispatchers.io) {
            messageDao.markConversationRead(conversationId)
        }
    }

    /**
     * Decrypts a MessageEntity for display.
     * In alpha: shows placeholder since the full DR pipeline isn't wired.
     */
    private fun decryptMessage(entity: MessageEntity): UiMessage {
        return try {
            // Alpha: decrypt if we have the key, otherwise show placeholder
            val keyResult = KeyStoreManager.getMessageKey()
            val aad = "${entity.id}:${entity.conversationId}".toByteArray()

            val plaintext = when (keyResult) {
                is SecureResult.Success -> {
                    AesGcmCipher.decrypt(entity.encryptedContent, keyResult.value, aad)
                }
                is SecureResult.Failure -> null
            }

            val text = when (plaintext) {
                is SecureResult.Success ->
                    String(plaintext.value, Charsets.UTF_8)
                else ->
                    "[encrypted message — key exchange pending]"
            }

            UiMessage(
                id = entity.id,
                text = text,
                isOutgoing = entity.isOutgoing,
                timestamp = formatTimestamp(entity.timestamp),
                delivered = entity.delivered,
                verified = _uiState.value.verified
            )
        } catch (e: Exception) {
            Logger.e(TAG, "Decryption failed for ${entity.id}", e)
            UiMessage(
                id = entity.id,
                text = "[decryption error]",
                isOutgoing = entity.isOutgoing,
                timestamp = formatTimestamp(entity.timestamp),
                delivered = false
            )
        }
    }

    private fun formatTimestamp(epochMs: Long): String {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = epochMs
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = cal.get(java.util.Calendar.MINUTE)
        return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }
}
