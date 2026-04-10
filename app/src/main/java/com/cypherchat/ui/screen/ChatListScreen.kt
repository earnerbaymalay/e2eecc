package com.cypherchat.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cypherchat.ui.theme.*
import com.cypherchat.viewmodel.ChatListViewModel
import com.cypherchat.viewmodel.ContactUi
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onOpenChat: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val viewModel: ChatListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var showNewContactDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    if (showNewContactDialog) {
        NewContactDialog(
            onDismiss = { showNewContactDialog = false },
            onCreate = { name ->
                viewModel.createContact(name)
                showNewContactDialog = false
            }
        )
    }

    showDeleteDialog?.let { convId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete conversation") },
            text = { Text("This will permanently delete this conversation and all messages.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteContact(convId)
                    showDeleteDialog = null
                }) { Text("Delete", color = CipherRed) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") } }
        )
    }

    Scaffold(
        containerColor = CipherNavy,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Lock, null, tint = CipherTeal, modifier = Modifier.size(18.dp))
                        Text("Cypherchat", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, "Settings", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CipherNavy)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewContactDialog = true },
                containerColor = CipherTeal,
                contentColor = CipherBlack,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, "New contact")
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CipherTeal)
                }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("⚠️", style = MaterialTheme.typography.headlineLarge)
                        Text(uiState.error!!, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        TextButton(onClick = { viewModel.retry() }) { Text("Retry", color = CipherTeal) }
                    }
                }
            }
            uiState.contacts.isEmpty() -> EmptyState(Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.contacts, key = { it.conversationId }) { contact ->
                    val lastMsg = uiState.lastMessages[contact.conversationId] ?: "Tap to start a conversation"
                    ChatRow(
                        contact = contact,
                        lastMessage = lastMsg,
                        onLongClick = { showDeleteDialog = contact.conversationId },
                        onClick = { onOpenChat(contact.conversationId) }
                    )
                    Divider(color = CipherBorder.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 72.dp))
                }
            }
        }
    }
}

@Composable
private fun ChatRow(contact: ContactUi, lastMessage: String, onClick: () -> Unit, onLongClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(CipherCard), contentAlignment = Alignment.Center) {
            Text(text = contact.initial, style = MaterialTheme.typography.titleMedium, color = CipherTeal)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = contact.displayName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(text = lastMessage, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🔒", style = MaterialTheme.typography.headlineLarge)
            Text("No conversations yet", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
            Text(
                "Tap + to add a contact.\nYou'll get an invitation link to share.",
                style = MaterialTheme.typography.bodyMedium, color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun NewContactDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Contact") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Contact name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("This creates a local contact. In production, this generates an invitation link to share securely.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onCreate(name.trim()) }
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
