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
                    ChatListUiState(contacts = contactUis, lastMessages = previews, isLoading = false)
                }.catch { e ->
                    Logger.e(TAG, "Error observing contacts", e)
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load conversations") }
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Fatal error in observeContacts", e)
                _uiState.update { it.copy(isLoading = false, error = "Failed to load conversations") }
            }
        }
    }

    fun createContact(displayName: String) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val conversationId = UUID.randomUUID().toString()
                val contact = ContactEntity(
                    id = UUID.randomUUID().toString(),
                    displayName = displayName,
                    publicKeyFingerprint = "pending",
                    publicKeyBytes = ByteArray(0),
                    conversationId = conversationId,
                    createdAt = System.currentTimeMillis(),
                    verified = false
                )
                contactDao.insert(contact)
                Logger.d(TAG, "Created contact: $displayName")
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to create contact", e)
                _uiState.update { it.copy(error = "Failed to create contact") }
            }
        }
    }

    fun deleteContact(conversationId: String) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val contact = contactDao.getByConversationId(conversationId)
                contact?.let { contactDao.deleteById(it.id) }
                messageDao.deleteConversation(conversationId)
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to delete contact", e)
            }
        }
    }

    fun retry() {
        _uiState.update { it.copy(error = null, isLoading = true) }
        observeContacts()
    }
}
