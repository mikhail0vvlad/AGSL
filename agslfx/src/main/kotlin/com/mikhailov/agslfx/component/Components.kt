package com.mikhailov.agslfx.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.RectangleShape
import com.mikhailov.agslfx.effect.shimmer

/**
 * Скелетон-заглушка: залитый прямоугольник с бегущим бликом.
 *
 * @param shape форма заглушки.
 * @param baseColor цвет подложки.
 */
@Composable
public fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    baseColor: Color = Color(0xFF23283A),
    highlight: Color = Color.White.copy(alpha = 0.35f),
    speed: Float = 0.7f,
) {
    Box(
        modifier
            .clip(shape)
            .shimmer(highlight = highlight, speed = speed)
            .background(baseColor)
    )
}
