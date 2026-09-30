package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GamingRed,
    onPrimary = Color.White,
    primaryContainer = GamingRedDark,
    onPrimaryContainer = Color(0xFFFFDADA),
    secondary = GamingGold,
    onSecondary = Color(0xFF241A00),
    secondaryContainer = GamingGoldDark,
    onSecondaryContainer = Color(0xFFFFEFA7),
    tertiary = GamingCyan,
    onTertiary = Color(0xFF002022),
    background = GamingDarkBackground,
    onBackground = GamingTextPrimary,
    surface = GamingCardSurface,
    onSurface = GamingTextPrimary,
    surfaceVariant = GamingCardSurfaceElevated,
    onSurfaceVariant = GamingTextSecondary,
    outline = GamingCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Keep branded gaming theme
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
