package com.cypherchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cypherchat.core.common.DispatcherProvider
import com.cypherchat.core.common.Logger
import com.cypherchat.core.common.SecureResult
import com.cypherchat.core.crypto.AesGcmCipher
import com.cypherchat.core.crypto.DoubleRatchetState
import com.cypherchat.core.crypto.KeyStoreManager
import com.cypherchat.core.database.dao.ContactDao
import com.cypherchat.core.database.dao.MessageDao
import com.cypherchat.core.database.entity.ContactEntity
import com.cypherchat.core.database.entity.MessageEntity
import com.cypherchat.core.network.IncomingEnvelope
import com.cypherchat.core.network.SimplexTransport
import com.cypherchat.core.network.SimplexTransportImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.util.UUID

private const val TAG = "ConversationViewModel"

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
    val error: String? = null,
    val isSending: Boolean = false,
    val connectionStatus: String = "Connected",
    val keyFingerprint: String = ""
)

class ConversationViewModel(
    private val conversationId: String,
    private val messageDao: MessageDao,
    private val contactDao: ContactDao,
    private val dispatchers: DispatcherProvider,
    private val transport: SimplexTransport = SimplexTransportImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    // Double Ratchet state for this conversation
    private var ratchetState: DoubleRatchetState? = null
    private lateinit var contactConnectionId: String

    init {
        observeMessages()
        loadContactInfo()
        observeIncomingMessages()
    }

    private fun observeIncomingMessages() {
        viewModelScope.launch(dispatchers.io) {
            transport.receiveMessages().collect { envelope ->
                handleIncomingMessage(envelope)
            }
        }
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
                        keyFingerprint = contact.publicKeyFingerprint,
                        verified = contact.verified
                    )
                }
                // Initialize Double Ratchet if we have the contact's public key
                if (contact.publicKeyBytes.isNotEmpty()) {
                    initializeRatchet(contact)
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

    private fun initializeRatchet(contact: ContactEntity) {
        // In production: Load saved ratchet state from DB or create from X3DH handshake
        // For now: Initialize with existing contact key
        viewModelScope.launch(dispatchers.io) {
            try {
                val myKeyPair = KeyPairGenerator.getInstance("EC").apply {
                    initialize(ECGenParameterSpec("secp256r1"))
                }.generateKeyPair()

                // Initialize as responder (Bob) with shared secret from Keystore
                val sharedSecretKey = KeyStoreManager.getMessageKey()
                    .getOrNull() ?: return@launch
                val sharedSecret = sharedSecretKey.encoded

                val result = DoubleRatchetState.initBob(
                    sharedSecret = sharedSecret,
                    bobRatchetKeyPair = myKeyPair,
                    // Would use contact's actual public key in production
                    aliceRatchetPublicKey = myKeyPair.public
                )

                if (result is SecureResult.Success) {
                    ratchetState = result.value
                    Logger.d(TAG, "Double Ratchet initialized for $conversationId")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to initialize ratchet", e)
            }
        }
    }

    /**
     * Sends a message: encrypts with Double Ratchet, stores, sends via transport.
     */
    fun sendMessage(plaintext: String) {
        if (plaintext.isBlank()) return

        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isSending = true, error = null) }

            val messageId = UUID.randomUUID().toString()

            // Encrypt with Double Ratchet if available, fallback to Keystore key
            val ciphertext = if (ratchetState != null) {
                val result = ratchetState!!.encryptMessage(plaintext.toByteArray(Charsets.UTF_8))
                when (result) {
                    is SecureResult.Success -> {
                        ratchetState = result.value.first
                        result.value.second
                    }
                    is SecureResult.Failure -> {
                        Logger.e(TAG, "Ratchet encrypt failed: ${result.error}")
                        _uiState.update {
                            it.copy(
                                error = "Encryption failed: ${result.error}",
                                isSending = false
                            )
                        }
                        return@launch
                    }
                }
            } else {
                // Fallback: use Keystore key (alpha mode)
                val keyResult = KeyStoreManager.getMessageKey()
                when (keyResult) {
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
            }

            if (ciphertext == null) {
                _uiState.update {
                    it.copy(error = "Encryption failed", isSending = false)
                }
                return@launch
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

            // Send via SimpleX transport
            // TODO: Use actual connection ID from SimpleX
            // transport.sendMessage(connection, ciphertext)

            Logger.d(TAG, "Message sent: $messageId (${plaintext.length} chars)")
            _uiState.update { it.copy(isSending = false) }
        }
    }

    /**
     * Handle incoming message from SimpleX transport.
     */
    private suspend fun handleIncomingMessage(envelope: IncomingEnvelope) {
        try {
            // Decrypt with Double Ratchet if available
            val plaintext = if (ratchetState != null) {
                // Would need sender's ratchet key from envelope header
                ratchetState!!.decryptMessage(
                    ciphertext = envelope.envelope,
                    // TODO: Extract sender's ratchet key from envelope
                    senderRatchetKey = ratchetState!!.dhSendKeyPair.public,
                    msgNum = 0
                ).map { (newState, text) ->
                    ratchetState = newState
                    String(text, Charsets.UTF_8)
                }.getOrNull() ?: "[decryption failed]"
            } else {
                // Fallback: try Keystore key
                val keyResult = KeyStoreManager.getMessageKey()
                when (keyResult) {
                    is SecureResult.Success -> {
                        AesGcmCipher.decrypt(envelope.envelope, keyResult.value)
                            .map { String(it, Charsets.UTF_8) }
                            .getOrNull() ?: "[decryption failed]"
                    }
                    is SecureResult.Failure -> "[no key available]"
                }
            }

            // Store incoming message
            val entity = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                senderKeyFingerprint = "remote_key",
                encryptedContent = envelope.envelope,
                timestamp = envelope.receivedAt,
                isOutgoing = false,
                delivered = true
            )
            messageDao.insert(entity)

            Logger.d(TAG, "Incoming message received and stored")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to handle incoming message", e)
        }
    }

    fun markRead() {
        viewModelScope.launch(dispatchers.io) {
            messageDao.markConversationRead(conversationId)
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
