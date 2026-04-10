package com.cypherchat.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
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
    var showFingerprint by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Show snackbar on error
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // Scroll to bottom when messages change
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(uiState.messages.lastIndex) }
        }
    }

    // Mark as read when leaving
    DisposableEffect(Unit) {
        onDispose { viewModel.markRead() }
    }

    Scaffold(
        containerColor = CipherNavy,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Animated encryption dot
                            if (uiState.connectionStatus == "Connected") {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(CipherTeal, CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(CipherOrange, CircleShape)
                                )
                            }
                            Text(
                                text = uiState.connectionStatus,
                                style = MaterialTheme.typography.labelSmall,
                                color = CipherTeal
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showFingerprint = !showFingerprint }) {
                        Icon(Icons.Default.Key, "Show fingerprint", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CipherNavy)
            )
        },
        bottomBar = {
            Column {
                // Fingerprint banner (slide up)
                AnimatedVisibility(
                    visible = showFingerprint,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = CipherSurface,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Security code",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                uiState.keyFingerprint.ifEmpty { "Not yet generated" },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                ),
                                color = CipherTeal,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Compare this with your contact to verify",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Message input bar
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
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message", color = TextMuted) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CipherTeal,
                            unfocusedBorderColor = CipherBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = CipherTeal,
                            focusedContainerColor = CipherCard,
                            unfocusedContainerColor = CipherCard
                        ),
                        maxLines = 4,
                        enabled = !uiState.isSending,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSend = {
                                if (input.isNotBlank()) {
                                    viewModel.sendMessage(input)
                                    input = ""
                                }
                            }
                        )
                    )

                    // Send button with loading state
                    IconButton(
                        onClick = {
                            if (input.isNotBlank() && !uiState.isSending) {
                                viewModel.sendMessage(input)
                                input = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (input.isNotBlank()) CipherTeal else CipherCard,
                                CircleShape
                            ),
                        enabled = input.isNotBlank() && !uiState.isSending
                    ) {
                        if (uiState.isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = CipherBlack
                            )
                        } else {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (input.isNotBlank()) CipherBlack else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                EncryptionNotice()
                Spacer(Modifier.height(8.dp))
            }

            // Loading state
            if (uiState.isLoading && uiState.messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CipherTeal)
                    }
                }
            }

            // Empty state
            if (uiState.messages.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyConversation()
                }
            }

            // Messages
            items(uiState.messages, key = { it.id }) { msg ->
                MessageBubble(msg)
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
        Text("🔒", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "No messages yet",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Messages are encrypted before they leave your device",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            textAlign = TextAlign.Center
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Lock, null, tint = CipherTeal, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Messages are end-to-end encrypted and cannot be read by anyone else",
            style = MaterialTheme.typography.labelSmall,
            color = CipherTeal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MessageBubble(message: UiMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (message.isOutgoing) 18.dp else 4.dp,
                            bottomEnd = if (message.isOutgoing) 4.dp else 18.dp
                        )
                    )
                    .background(
                        if (message.text.startsWith("[")) CipherErrorBg else if (message.isOutgoing) BubbleOutgoing else BubbleIncoming
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (message.text.startsWith("[")) CipherOrange else TextPrimary
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                if (message.isOutgoing) {
                    Text(
                        text = if (message.delivered) "✓✓" else "✓",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.delivered) CipherTeal else TextMuted
                    )
                }
            }
        }
    }
}
