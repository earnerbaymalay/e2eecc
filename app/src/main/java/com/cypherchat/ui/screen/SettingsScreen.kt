package com.cypherchat.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cypherchat.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = CipherNavy,
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { androidx.compose.material3.Icon(Icons.Default.ArrowBack, null, tint = TextSecondary) } },
                title = { Text("Settings", color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CipherNavy)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                InfoCard(title = "Encryption", desc = "AES-256-GCM + Double Ratchet\nAll messages encrypted before leaving your device")
            }
            item {
                InfoCard(title = "Database", desc = "SQLCipher (AES-256-CBC)\nDatabase encrypted at rest")
            }
            item {
                InfoCard(title = "Key Storage", desc = "Android Keystore (hardware-backed)\nKeys never leave the secure element")
            }
            item {
                InfoCard(title = "Accounts", desc = "None required — no phone numbers\nno usernames, no servers")
            }
            item {
                InfoCard(title = "Status", desc = "Alpha — SimpleX transport not yet integrated.\nMessages encrypt locally but don't transmit.")
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CipherCard)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}
