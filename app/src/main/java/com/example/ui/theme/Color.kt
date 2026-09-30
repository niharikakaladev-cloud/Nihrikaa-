package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Gaming Brand Colors (Dark Crimson, Obsidian, Gold Highlights)
val GamingRed = Color(0xFFFF2442)
val GamingRedDark = Color(0xFFC00A23)
val GamingRedLight = Color(0xFFFF596E)
val GamingGold = Color(0xFFFFC727)
val GamingGoldDark = Color(0xFFD49E00)
val GamingCyan = Color(0xFF00E5FF)
val GamingEmerald = Color(0xFF00E676)

// Dark Surfaces
val GamingDarkBackground = Color(0xFF0A090F)
val GamingCardSurface = Color(0xFF14131D)
val GamingCardSurfaceElevated = Color(0xFF1D1B2A)
val GamingCardBorder = Color(0xFF2E2A40)
val GamingTextPrimary = Color(0xFFF6F6FC)
val GamingTextSecondary = Color(0xFF9E9DB5)
val GamingTextMuted = Color(0xFF67657F)

// Gradients
val GamingRedGradient = Brush.horizontalGradient(
    colors = listOf(GamingRed, Color(0xFFFF5A36))
)

val GamingGoldGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFFDF00), Color(0xFFFF9900))
)

val GamingCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1C1A29), Color(0xFF12111A))
)

val GamingDarkBackdropGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1E0A12), Color(0xFF0A090F), Color(0xFF08070C))
)
