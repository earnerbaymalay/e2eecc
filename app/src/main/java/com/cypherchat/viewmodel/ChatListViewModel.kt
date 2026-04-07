package com.cypherchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cypherchat.core.common.DispatcherProvider
import com.cypherchat.core.common.Logger
import com.cypherchat.core.database.dao.ContactDao
import com.cypherchat.core.database.dao.MessageDao
import com.cypherchat.core.database.entity.ContactEntity
import com.cypherchat.core.database.entity.MessageEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "ChatListViewModel"

/**
 * UI state for the chat list screen.
 */
data class ChatListUiState(
    val contacts: List<ContactUi> = emptyList(),
    val lastMessages: Map<String, String> = emptyMap(),  // conversationId → preview
    val unreadCounts: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true
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
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    /**
     * Creates a new contact entry (stub — wire to SimpleX invitation in network phase).
     */
    fun createNewConversation(displayName: String) {
        viewModelScope.launch(dispatchers.io) {
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
            Logger.d(TAG, "Created conversation: $conversationId for $displayName")
        }
    }

    fun markConversationRead(conversationId: String) {
        viewModelScope.launch(dispatchers.io) {
            messageDao.markConversationRead(conversationId)
        }
    }
}
