package com.cypherchat.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Cypherchat always uses a dark theme — privacy-first apps don't do light mode
private val DarkColors = darkColorScheme(
    primary            = CipherBlue,
    onPrimary          = CipherDarkBg,
    primaryContainer   = CipherSurface,
    onPrimaryContainer = CipherOnSurface,
    secondary          = CipherAccent,
    onSecondary        = CipherDarkBg,
    background         = CipherDarkBg,
    onBackground       = CipherOnSurface,
    surface            = CipherSurface,
    onSurface          = CipherOnSurface,
    surfaceVariant     = CipherSurface,
    onSurfaceVariant   = TextSecondary,
    outline            = CipherBorder,
    error              = CipherError,
    onError            = CipherOnSurface,
)

@Composable
fun Cyph3rChatTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColors,
        typography  = CypherchatTypography,
        content     = content
    )
}
