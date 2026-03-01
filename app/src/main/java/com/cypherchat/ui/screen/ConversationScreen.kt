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
import kotlinx.coroutines.launch

// Placeholder message model — wire to Room/ViewModel in next phase
private data class Message(
    val id: String,
    val text: String,
    val isOutgoing: Boolean,
    val timestamp: String,
    val delivered: Boolean = true
)

private val PLACEHOLDER_MESSAGES = listOf(
    Message("1", "Hey — got your invitation link. Setting up now.", false, "14:02"),
    Message("2", "Great! Let me know when you're ready to test the connection.", true, "14:03"),
    Message("3", "Ready. This feels instant and the key fingerprints match.", false, "14:05"),
    Message("4", "Confirmed on my end too. We're good. 🔒", true, "14:05", delivered = true),
    Message("5", "I'll share the design files through here from now on.", false, "14:07"),
    Message("6", "Works for me. Much better than email.", true, "14:08"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    conversationId: String,
    onBack: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope     = rememberCoroutineScope()

    // Scroll to bottom on initial load
    LaunchedEffect(Unit) {
        if (PLACEHOLDER_MESSAGES.isNotEmpty()) {
            listState.scrollToItem(PLACEHOLDER_MESSAGES.lastIndex)
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
                            "Alex K",    // TODO: resolve from conversationId
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint               = CipherTeal,
                                modifier           = Modifier.size(10.dp)
                            )
                            Text(
                                "End-to-end encrypted",
                                style = MaterialTheme.typography.labelSmall,
                                color = CipherTeal
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
                            // TODO: encrypt + send via ViewModel
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

            items(PLACEHOLDER_MESSAGES) { msg ->
                MessageBubble(msg)
            }
        }
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
private fun MessageBubble(message: Message) {
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
