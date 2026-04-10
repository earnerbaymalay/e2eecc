package com.cypherchat.ui.screen

import androidx.compose.animation.*
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
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CipherNavy
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.createNewConversation() },
                containerColor = CipherTeal,
                contentColor = CipherBlack,
                shape = RoundedCornerShape(14.dp)
            ) {
                AnimatedVisibility(
                    visible = uiState.isCreatingInvite,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = CipherBlack
                    )
                }
                AnimatedVisibility(
                    visible = !uiState.isCreatingInvite,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New chat")
                }
            }
        }
    ) { padding ->
        // Loading state
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CipherTeal)
            }
        }
        // Error state
        else if (uiState.error != null) {
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
                    Text(
                        uiState.error ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    TextButton(onClick = { viewModel.retry() }) {
                        Text("Retry", color = CipherTeal)
                    }
                }
            }
        }
        // Empty state
        else if (uiState.contacts.isEmpty()) {
            EmptyState(modifier = Modifier.padding(padding))
        }
        // Contact list
        else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.contacts, key = { it.conversationId }) { contact ->
                    val lastMsg = uiState.lastMessages[contact.conversationId] ?: "Start a conversation"
                    val unread = uiState.unreadCounts[contact.conversationId] ?: 0
                    ChatRow(
                        contact = contact,
                        lastMessage = lastMsg,
                        unreadCount = unread,
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

@Composable
private fun ChatRow(contact: ContactUi, lastMessage: String, unreadCount: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Avatar
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

        // Content
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

                // Timestamp + unread badge row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = formatTimestamp(contact.lastSeen),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (unreadCount > 0) CipherTeal else TextMuted
                    )
                    if (unreadCount > 0) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = CipherBlack,
                            modifier = Modifier
                                .background(CipherTeal, CircleShape)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Text(
                text = lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = if (unreadCount > 0) TextPrimary else TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = if (timestamp > 0) now - timestamp else Long.MAX_VALUE
    return when {
        diff < 60_000 -> "now"
        diff < 3600_000 -> "${diff / 60_000}m"
        diff < 86400_000 -> "${diff / 3600_000}h"
        diff > 30_000_000_000L -> ""
        else -> android.text.format.DateFormat.format("MMM d", timestamp).toString()
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
