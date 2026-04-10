package com.cypherchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cypherchat.core.common.DispatcherProvider
import com.cypherchat.core.common.Logger
import com.cypherchat.core.database.dao.ContactDao
import com.cypherchat.core.database.dao.MessageDao
import com.cypherchat.core.database.entity.ContactEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "ChatListViewModel"

data class ChatListUiState(
    val contacts: List<ContactUi> = emptyList(),
    val lastMessages: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    val isCreatingInvite: Boolean = false,
    val error: String? = null
)

data class ContactUi(
    val id: String,
    val displayName: String,
    val conversationId: String,
    val verified: Boolean,
    val lastSeen: Long,
    val initial: String
)

class ChatListViewModel(
    private val contactDao: ContactDao,
    private val messageDao: MessageDao,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        observeContacts()
    }

    private fun observeContacts() {
        viewModelScope.launch(dispatchers.io) {
            try {
                combine(
                    contactDao.observeAll(),
                    messageDao.observeAllLastMessages()
                ) { contacts, lastMsgs ->
                    val contactUis = contacts.map { c ->
                        ContactUi(
                            id = c.id,
                            displayName = c.displayName,
                            conversationId = c.conversationId,
                            verified = c.verified,
                            lastSeen = c.lastSeen,
                            initial = c.displayName.take(1).uppercase()
                        )
                    }
                    val previews = lastMsgs.associate { it.conversationId to it.previewText }

                    ChatListUiState(
                        contacts = contactUis,
                        lastMessages = previews,
                        isLoading = false
                    )
                }.catch { e ->
                    Logger.e(TAG, "Error observing contacts", e)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load conversations"
                        )
                    }
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Fatal error in observeContacts", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load conversations: ${e.message}"
                    )
                }
            }
        }
    }

    /** Create a new contact with an invitation link. */
    fun createNewConversation() {
        createNewConversation("New Contact")
    }

    fun createNewConversation(displayName: String) {
        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isCreatingInvite = true, error = null) }
            try {
                // Create a stub contact for alpha (full SimpleX integration planned)
                val conversationId = UUID.randomUUID().toString()
                val contact = ContactEntity(
                    id = UUID.randomUUID().toString(),
                    displayName = displayName,
                    publicKeyFingerprint = "pending_key_exchange",
                    publicKeyBytes = ByteArray(0),
                    conversationId = conversationId,
                    createdAt = System.currentTimeMillis(),
                    verified = false
                )
                contactDao.insert(contact)
                _uiState.update { it.copy(isCreatingInvite = false) }
                Logger.d(TAG, "Created contact: $displayName")
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to create contact", e)
                _uiState.update {
                    it.copy(
                        isCreatingInvite = false,
                        error = "Failed to create contact: ${e.message}"
                    )
                }
            }
        }
    }

    fun retry() {
        _uiState.update { it.copy(error = null, isLoading = true) }
        observeContacts()
    }

    fun markConversationRead(conversationId: String) {
        viewModelScope.launch(dispatchers.io) {
            try {
                messageDao.markConversationRead(conversationId)
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to mark conversation read", e)
            }
        }
    }
}
