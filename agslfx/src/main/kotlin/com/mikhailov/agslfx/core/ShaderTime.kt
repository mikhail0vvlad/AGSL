package com.mikhailov.agslfx.core

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember

/**
 * Кадровый источник времени для шейдеров.
 *
 * Значение обновляется каждый кадр в отдельном [State], поэтому чтение его внутри
 * `graphicsLayer { }` или `onDraw { }` перерисовывает слой **без рекомпозиции** —
 * это главный трюк, который делает анимированные шейдеры дешёвыми.
 *
 * Используется [withInfiniteAnimationFrameMillis], а значит время автоматически
 * замирает там, где Compose останавливает бесконечные анимации (тесты, `@Preview`).
 *
 * @param enabled если false — время замирает на текущем значении.
 * @param speed множитель скорости.
 */
@Composable
public fun rememberShaderTime(
    enabled: Boolean = true,
    speed: Float = 1f,
): State<Float> {
    val time = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(enabled, speed) {
        if (!enabled) return@LaunchedEffect
        var startMillis = -1L
        var offset = time.floatValue
        while (true) {
            withInfiniteAnimationFrameMillis { frameMillis ->
                if (startMillis < 0L) startMillis = frameMillis
                time.floatValue = offset + (frameMillis - startMillis) / 1000f * speed
            }
        }
    }
    return time
}
