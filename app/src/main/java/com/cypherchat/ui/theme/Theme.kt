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
    primary            = CipherTeal,
    onPrimary          = CipherBlack,
    primaryContainer   = CipherTealDim,
    onPrimaryContainer = TextPrimary,
    secondary          = CipherTealDim,
    onSecondary        = TextPrimary,
    background         = CipherNavy,
    onBackground       = TextPrimary,
    surface            = CipherSurface,
    onSurface          = TextPrimary,
    surfaceVariant     = CipherCard,
    onSurfaceVariant   = TextSecondary,
    outline            = CipherBorder,
    error              = CipherRed,
    onError            = TextPrimary,
)

@Composable
fun CypherchatTheme(content: @Composable () -> Unit) {
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
