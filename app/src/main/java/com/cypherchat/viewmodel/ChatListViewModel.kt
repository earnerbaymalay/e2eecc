package com.cypherchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cypherchat.core.common.DispatcherProvider
import com.cypherchat.core.common.Logger
import com.cypherchat.core.common.SecureResult
import com.cypherchat.core.database.dao.ContactDao
import com.cypherchat.core.database.dao.MessageDao
import com.cypherchat.core.database.entity.ContactEntity
import com.cypherchat.core.network.SimplexInvitation
import com.cypherchat.core.network.SimplexTransport
import com.cypherchat.core.network.SimplexTransportImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "ChatListViewModel"

data class ChatListUiState(
    val contacts: List<ContactUi> = emptyList(),
    val lastMessages: Map<String, String> = emptyMap(),
    val unreadCounts: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val isCreatingInvite: Boolean = false,
    val currentInvitation: SimplexInvitation? = null,
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
    private val dispatchers: DispatcherProvider,
    private val transport: SimplexTransport = SimplexTransportImpl()
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
                    isLoading = false,
                    currentInvitation = _uiState.value.currentInvitation,
                    isCreatingInvite = _uiState.value.isCreatingInvite
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    /**
     * Create a new contact with SimpleX invitation.
     * Generates an invitation link that can be shared.
     */
    fun createNewConversation(displayName: String) {
        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isCreatingInvite = true, error = null) }

            // Create SimpleX invitation
            val invitationResult = transport.createInvitation()
            
            when (invitationResult) {
                is SecureResult.Success -> {
                    val invitation = invitationResult.value
                    val conversationId = UUID.randomUUID().toString()
                    
                    // Create contact with invitation data
                    val contact = ContactEntity(
                        id = UUID.randomUUID().toString(),
                        displayName = displayName,
                        publicKeyFingerprint = invitation.publicKey.take(8).joinToString("") { "%02x".format(it) },
                        publicKeyBytes = invitation.publicKey,
                        conversationId = conversationId,
                        createdAt = System.currentTimeMillis(),
                        verified = false
                    )
                    contactDao.insert(contact)

                    _uiState.update {
                        it.copy(
                            isCreatingInvite = false,
                            currentInvitation = invitation
                        )
                    }

                    Logger.d(TAG, "Created invitation for $displayName: $conversationId")
                }
                is SecureResult.Failure -> {
                    // Fallback: create without invitation (alpha mode)
                    Logger.w(TAG, "Invitation creation failed, creating stub: ${invitationResult.error}")
                    createStubContact(displayName, conversationId = UUID.randomUUID().toString())
                }
            }
        }
    }

    /**
     * Accept an incoming invitation link.
     */
    fun acceptInvitation(invitationLink: String, displayName: String) {
        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isCreatingInvite = true, error = null) }

            // TODO: Parse invitation link and accept via transport
            // For now: Create contact from link data
            createStubContact(displayName, conversationId = UUID.randomUUID().toString())
            
            _uiState.update { it.copy(isCreatingInvite = false) }
            Logger.d(TAG, "Accepted invitation from link")
        }
    }

    /**
     * Create a stub contact (fallback when invitation system isn't available).
     */
    private suspend fun createStubContact(displayName: String, conversationId: String) {
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
        Logger.d(TAG, "Created stub contact: $displayName")
    }

    fun clearInvitation() {
        _uiState.update { it.copy(currentInvitation = null) }
    }

    fun markConversationRead(conversationId: String) {
        viewModelScope.launch(dispatchers.io) {
            messageDao.markConversationRead(conversationId)
        }
    }
}
