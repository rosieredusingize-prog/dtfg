package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DTFGColorScheme = darkColorScheme(
    primary = DTFGCyan,
    onPrimary = Color(0xFF041021),
    primaryContainer = DTFGSurfaceVariant,
    onPrimaryContainer = DTFGTextPrimary,
    secondary = DTFGBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF13274A),
    onSecondaryContainer = DTFGTextPrimary,
    tertiary = DTFGEmerald,
    onTertiary = Color(0xFF003822),
    background = DTFGDarkBg,
    onBackground = DTFGTextPrimary,
    surface = DTFGSurface,
    onSurface = DTFGTextPrimary,
    surfaceVariant = DTFGSurfaceVariant,
    onSurfaceVariant = DTFGTextSecondary,
    outline = DTFGBorder,
    outlineVariant = Color(0xFF182A48)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DTFGColorScheme,
        typography = DTFGTypography,
        content = content
    )
}
