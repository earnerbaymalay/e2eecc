package com.cypherchat.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cypherchat.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CipherNavy),
        contentAlignment = Alignment.Center
    ) {
        // Subtle radial glow at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        colors  = listOf(CipherTealSoft, androidx.compose.ui.graphics.Color.Transparent),
                        radius  = 600f
                    )
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
            ) {
                // Lock icon placeholder — swap with actual vector asset
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(CipherTealSoft, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔒", style = MaterialTheme.typography.headlineLarge)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text  = "Cypherchat",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )

                Text(
                    text  = "Zero-knowledge messaging.\nNo accounts. No metadata. No compromise.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )

                Spacer(Modifier.height(8.dp))

                // Feature list
                listOf(
                    "End-to-end encrypted with Double Ratchet",
                    "No user IDs — invite links only",
                    "All data stays on your device",
                    "Open source, no ads, no tracking"
                ).forEach { feature ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(CipherTeal, RoundedCornerShape(50))
                        )
                        Text(
                            text  = feature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CipherTeal,
                        contentColor   = CipherBlack
                    )
                ) {
                    Text(
                        text  = "Get Started",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Text(
                    text  = "Your identity is generated locally.\nNothing leaves your device.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
