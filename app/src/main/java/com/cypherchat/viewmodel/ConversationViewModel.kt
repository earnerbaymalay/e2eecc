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
import com.cypherchat.core.database.entity.MessageEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "ConversationViewModel"

data class UiMessage(
    val id: String,
    val text: String,
    val isOutgoing: Boolean,
    val timestamp: String,
    val delivered: Boolean = false
)

data class ConversationUiState(
    val messages: List<UiMessage> = emptyList(),
    val contactName: String = "Unknown",
    val verified: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSending: Boolean = false,
    val keyFingerprint: String = ""
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
            try {
                messageDao.observeMessages(conversationId)
                    .map { entities -> entities.map { decryptMessage(it) } }
                    .catch { e ->
                        Logger.e(TAG, "Error observing messages", e)
                        _uiState.update {
                            it.copy(isLoading = false, error = "Failed to load messages")
                        }
                    }
                    .collect { messages ->
                        _uiState.update { it.copy(messages = messages, isLoading = false) }
                    }
            } catch (e: Exception) {
                Logger.e(TAG, "Fatal error in observeMessages", e)
                _uiState.update { it.copy(isLoading = false, error = "Failed to load messages") }
            }
        }
    }

    private fun loadContactInfo() {
        viewModelScope.launch(dispatchers.io) {
            try {
                val contact = contactDao.getByConversationId(conversationId)
                if (contact != null) {
                    _uiState.update {
                        it.copy(
                            contactName = contact.displayName,
                            keyFingerprint = contact.publicKeyFingerprint,
                            verified = contact.verified,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(contactName = "New Conversation", isLoading = false) }
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to load contact", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /** Sends a message encrypted with the Keystore key (alpha mode). */
    fun sendMessage(plaintext: String) {
        if (plaintext.isBlank()) return

        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isSending = true, error = null) }

            try {
                val keyResult = KeyStoreManager.getMessageKey()
                val ciphertext = when (keyResult) {
                    is SecureResult.Success -> {
                        AesGcmCipher.encrypt(
                            plaintext.toByteArray(Charsets.UTF_8),
                            keyResult.value
                        ).getOrNull()
                    }
                    is SecureResult.Failure -> {
                        Logger.e(TAG, "Failed to get message key: ${keyResult.error}")
                        null
                    }
                }

                if (ciphertext == null) {
                    _uiState.update {
                        it.copy(error = "Encryption failed", isSending = false)
                    }
                    return@launch
                }

                val entity = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    conversationId = conversationId,
                    senderKeyFingerprint = "local_key",
                    encryptedContent = ciphertext,
                    timestamp = System.currentTimeMillis(),
                    isOutgoing = true,
                    delivered = true
                )
                messageDao.insert(entity)
                _uiState.update { it.copy(isSending = false) }
                Logger.d(TAG, "Message sent")
            } catch (e: Exception) {
                Logger.e(TAG, "Send failed", e)
                _uiState.update { it.copy(error = "Send failed: ${e.message}", isSending = false) }
            }
        }
    }

    fun markRead() {
        viewModelScope.launch(dispatchers.io) {
            try {
                messageDao.markConversationRead(conversationId)
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to mark read", e)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun decryptMessage(entity: MessageEntity): UiMessage {
        return try {
            val keyResult = KeyStoreManager.getMessageKey()
            val plaintext = when (keyResult) {
                is SecureResult.Success -> {
                    AesGcmCipher.decrypt(entity.encryptedContent, keyResult.value)
                }
                is SecureResult.Failure -> null
            }

            val text = when (plaintext) {
                is SecureResult.Success -> String(plaintext.value, Charsets.UTF_8)
                else -> "[encrypted message]"
            }

            UiMessage(
                id = entity.id,
                text = text,
                isOutgoing = entity.isOutgoing,
                timestamp = formatTimestamp(entity.timestamp),
                delivered = entity.delivered
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
