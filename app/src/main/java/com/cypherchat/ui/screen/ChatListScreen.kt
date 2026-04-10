package com.cypherchat.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cypherchat.ui.theme.*
import com.cypherchat.viewmodel.ChatListViewModel
import com.cypherchat.viewmodel.ContactUi
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(onOpenChat: (String) -> Unit) {
    val viewModel: ChatListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = CipherNavy,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = CipherTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Cypherchat",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CipherNavy)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.createNewConversation() },
                containerColor = CipherTeal,
                contentColor = CipherBlack,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New chat")
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CipherTeal)
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("⚠️", style = MaterialTheme.typography.headlineLarge)
                        Text(uiState.error!!, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        TextButton(onClick = { viewModel.retry() }) {
                            Text("Retry", color = CipherTeal)
                        }
                    }
                }
            }
            uiState.contacts.isEmpty() -> {
                EmptyState(modifier = Modifier.padding(padding))
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.contacts, key = { it.conversationId }) { contact ->
                        val lastMsg = uiState.lastMessages[contact.conversationId]
                            ?: "Tap to start a conversation"
                        ChatRow(
                            contact = contact,
                            lastMessage = lastMsg,
                            onClick = { onOpenChat(contact.conversationId) }
                        )
                        Divider(
                            color = CipherBorder.copy(alpha = 0.5f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 72.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatRow(contact: ContactUi, lastMessage: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(CipherCard),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.initial,
                style = MaterialTheme.typography.titleMedium,
                color = CipherTeal
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = contact.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    if (contact.verified) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(CipherTeal, CircleShape)
                        )
                    }
                }
            }

            Text(
                text = lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🔒", style = MaterialTheme.typography.headlineLarge)
            Text(
                "No conversations yet",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )
            Text(
                "Tap + to create an invitation link\nand share it securely with a contact",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
