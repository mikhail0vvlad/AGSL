package com.mikhailov.agslfx.effect

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

/**
 * Интерактивная волна по касанию: тап запускает расходящуюся рябь из точки нажатия.
 *
 * @param amplitudePx максимальное смещение пикселей.
 * @param durationMillis длительность волны.
 * @param onTap дополнительный обработчик тапа.
 */
@Composable
public fun Modifier.touchRipple(
    amplitudePx: Float = 30f,
    durationMillis: Int = 900,
    frequency: Float = 12f,
    onTap: (Offset) -> Unit = {},
): Modifier {
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(1f) }
    var center by remember { mutableStateOf(Offset.Zero) }

    return this
        .pointerInput(durationMillis) {
            detectTapGestures { offset ->
                center = offset
                onTap(offset)
                scope.launch {
                    progress.snapTo(0f)
                    progress.animateTo(1f, tween(durationMillis, easing = LinearOutSlowInEasing))
                }
            }
        }
        .waterRipple(
            center = center,
            progress = progress.value,
            amplitudePx = amplitudePx,
            frequency = frequency,
        )
}
