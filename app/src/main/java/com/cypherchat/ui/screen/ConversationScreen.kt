package com.cypherchat.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cypherchat.ui.theme.*
import com.cypherchat.viewmodel.ConversationViewModel
import com.cypherchat.viewmodel.UiMessage
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    conversationId: String,
    onBack: () -> Unit
) {
    val viewModel: ConversationViewModel = koinViewModel { parametersOf(conversationId) }
    val uiState by viewModel.uiState.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope     = rememberCoroutineScope()

    // Scroll to bottom when messages change
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.scrollToItem(uiState.messages.lastIndex)
        }
    }

    Scaffold(
        containerColor = CipherNavy,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextSecondary)
                    }
                },
                title = {
                    Column {
                        Text(
                            uiState.contactName,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        )
                        {
                            Icon(
                                imageVector        = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint               = CipherTeal,
                                modifier           = Modifier.size(10.dp)
                            )
                            Text(
                                if (uiState.verified) "Verified • End-to-end encrypted" else "End-to-end encrypted",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.verified) CipherGreen else CipherTeal
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CipherNavy)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CipherSurface)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value         = input,
                    onValueChange = { input = it },
                    modifier      = Modifier.weight(1f),
                    placeholder   = { Text("Message", color = TextMuted) },
                    shape         = RoundedCornerShape(24.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = CipherTeal,
                        unfocusedBorderColor = CipherBorder,
                        focusedTextColor     = TextPrimary,
                        unfocusedTextColor   = TextPrimary,
                        cursorColor          = CipherTeal,
                        focusedContainerColor   = CipherCard,
                        unfocusedContainerColor = CipherCard
                    ),
                    maxLines = 4
                )

                IconButton(
                    onClick  = {
                        if (input.isNotBlank()) {
                            viewModel.sendMessage(input)
                            input = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (input.isNotBlank()) CipherTeal else CipherCard,
                            RoundedCornerShape(50)
                        )
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (input.isNotBlank()) CipherBlack else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state         = listState,
            modifier      = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Encryption notice at top
            item {
                EncryptionNotice()
                Spacer(Modifier.height(8.dp))
            }

            if (uiState.messages.isEmpty()) {
                item {
                    EmptyConversation()
                }
            } else {
                items(uiState.messages) { msg ->
                    MessageBubble(msg)
                }
            }
        }
    }
}

@Composable
private fun EmptyConversation() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "🔒",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "No messages yet",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
    }
}

@Composable
private fun EncryptionNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CipherTealSoft)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Lock,
            contentDescription = null,
            tint     = CipherTeal,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text  = "Messages are end-to-end encrypted and cannot be read by anyone else",
            style = MaterialTheme.typography.labelSmall,
            color = CipherTeal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MessageBubble(message: UiMessage) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (message.isOutgoing) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart    = 18.dp,
                            topEnd      = 18.dp,
                            bottomStart = if (message.isOutgoing) 18.dp else 4.dp,
                            bottomEnd   = if (message.isOutgoing) 4.dp else 18.dp
                        )
                    )
                    .background(if (message.isOutgoing) BubbleOutgoing else BubbleIncoming)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text  = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text  = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                if (message.isOutgoing) {
                    Text(
                        text  = if (message.delivered) "✓✓" else "✓",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.delivered) CipherTeal else TextMuted
                    )
                }
            }
        }
    }
}
