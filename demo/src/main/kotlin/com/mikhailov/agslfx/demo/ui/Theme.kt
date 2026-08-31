package com.mikhailov.agslfx.demo.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFCC00),
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFFFC3F1D),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF141414),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1F1F1F),
    onSurfaceVariant = Color(0xFFA6A6A6),
    outline = Color(0xFF333333),
)

@Composable
fun AgslFxTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
