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

// Placeholder data for UI development — wire to ViewModel + Room in next phase
private data class ChatPreview(
    val conversationId: String,
    val contactName: String,
    val lastMessagePreview: String,
    val timestamp: String,
    val unread: Int,
    val verified: Boolean
)

private val PLACEHOLDER_CHATS = listOf(
    ChatPreview("conv1", "Alex K", "Sounds good — I'll send the files now", "now", 2, true),
    ChatPreview("conv2", "Morgan", "Thanks for sharing that link", "2m", 0, true),
    ChatPreview("conv3", "Unknown Contact", "Hey, got your invite — this is me!", "1h", 1, false),
    ChatPreview("conv4", "Dev Team", "Build is passing on main ✓", "3h", 0, true),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(onOpenChat: (String) -> Unit) {
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
                            imageVector        = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint               = CipherTeal,
                            modifier           = Modifier.size(18.dp)
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
                onClick           = { /* TODO: show new chat / invitation flow */ },
                containerColor    = CipherTeal,
                contentColor      = CipherBlack,
                shape             = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New chat")
            }
        }
    ) { padding ->
        if (PLACEHOLDER_CHATS.isEmpty()) {
            EmptyState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier      = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(PLACEHOLDER_CHATS) { chat ->
                    ChatRow(chat = chat, onClick = { onOpenChat(chat.conversationId) })
                    Divider(color = CipherBorder.copy(alpha = 0.5f), thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 72.dp))
                }
            }
        }
    }
}

@Composable
private fun ChatRow(chat: ChatPreview, onClick: () -> Unit) {
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
                text  = chat.contactName.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = CipherTeal
            )
        }

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text  = chat.contactName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    if (chat.verified) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(CipherTeal, CircleShape)
                        )
                    }
                }
                Text(
                    text  = chat.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (chat.unread > 0) CipherTeal else TextMuted
                )
            }

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text     = chat.lastMessagePreview,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (chat.unread > 0) {
                    Badge(
                        containerColor = CipherTeal,
                        contentColor   = CipherBlack,
                        modifier       = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text  = chat.unread.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier          = modifier.fillMaxSize(),
        contentAlignment  = Alignment.Center
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
                style     = MaterialTheme.typography.bodyMedium,
                color     = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
