package com.mikhailov.agslfx.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.RectangleShape
import com.mikhailov.agslfx.effect.dissolve
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

/**
 * Показывает/прячет содержимое растворением по шумовой маске.
 *
 * @param visible целевое состояние.
 * @param seed индивидуальный сдвиг шума — задайте разный соседним элементам.
 */
@Composable
public fun DissolveVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    durationMillis: Int = 750,
    scale: Float = 6f,
    edgeWidth: Float = 0.09f,
    edgeColor: Color = Color(0xFFFF7A18),
    seed: Float = 0f,
    content: @Composable BoxScope.() -> Unit,
) {
    val progress by animateFloatAsState(
        targetValue = if (visible) 0f else 1f,
        animationSpec = tween(durationMillis),
        label = "dissolve",
    )
    if (progress < 0.999f) {
        Box(
            modifier = modifier.dissolve(
                progress = progress,
                scale = scale,
                edgeWidth = edgeWidth,
                edgeColor = edgeColor,
                seed = seed,
            ),
            content = content,
        )
    }
}
