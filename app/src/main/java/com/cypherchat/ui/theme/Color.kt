package com.cypherchat.ui.theme

import androidx.compose.ui.graphics.Color

// ── Cipherchat Brand Palette ──────────────────────────────────────────────
val CipherBlue      = Color(0xFF00D4FF)
val CipherDarkBg    = Color(0xFF0D1117)
val CipherSurface   = Color(0xFF161B22)
val CipherOnSurface = Color(0xFFC9D1D9)
val CipherAccent    = Color(0xFF58A6FF)
val CipherError     = Color(0xFFF85149)
val CipherSuccess   = Color(0xFF3FB950)

// ── Legacy / Component Mapping ──────────────────────────────────────────────
val CipherBlack     = Color(0xFF07090F)
val CipherNavy      = CipherDarkBg
val CipherCard      = Color(0xFF1E2535)
val CipherBorder    = Color(0xFF2A3145)

val CipherTeal      = CipherBlue
val CipherTealDim   = Color(0xFF008F72)
val CipherTealSoft  = Color(0xFF00D4FF26) // 15% alpha

val CipherAmber     = Color(0xFFFFA500)
val CipherRed       = CipherError
val CipherGreen     = CipherSuccess
val CipherOrange    = Color(0xFFFF8C00)
val CipherErrorBg   = Color(0xFFF8514915) // 8% error bg

val TextPrimary     = CipherOnSurface
val TextSecondary   = Color(0xFF8B93A8)
val TextMuted       = Color(0xFF4A5268)

// Bubble colors
val BubbleOutgoing  = Color(0xFF003D2E)   // Dark teal — outgoing messages
val BubbleIncoming  = Color(0xFF1E2535)   // Dark card — incoming messages
