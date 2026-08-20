package com.mikhailov.agslfx.demo.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FD3FF),
    onPrimary = Color(0xFF04121F),
    secondary = Color(0xFFC7A8FF),
    background = Color(0xFF07080F),
    onBackground = Color(0xFFE8EAF2),
    surface = Color(0xFF10121C),
    onSurface = Color(0xFFE8EAF2),
    surfaceVariant = Color(0xFF1A1D2B),
    onSurfaceVariant = Color(0xFFAAB0C4),
    outline = Color(0xFF39405A),
)

@Composable
fun AgslFxTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
