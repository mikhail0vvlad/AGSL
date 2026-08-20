package com.mikhailov.agslfx.effect

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikhailov.agslfx.core.AgslProgram
import com.mikhailov.agslfx.core.agslEffect

/** Плёночное зерно: аналоговый шум, который слабее в тенях и светах. */
public val FilmGrainProgram: AgslProgram = AgslProgram(
    name = "Film grain",
    body = """
uniform shader content;
uniform float uTime;

uniform float uIntensity;   // амплитуда зерна
uniform float uSize;        // размер зерна в пикселях
uniform float uMidtoneBias; // 0 — равномерно, 1 — только в полутонах
uniform float uFps;         // частота смены зерна

half4 main(float2 fragCoord) {
    half4 src = content.eval(fragCoord);

    float2 cell = floor(fragCoord / max(uSize, 1.0));
    float frame = floor(uTime * max(uFps, 1.0));
    float noise = agslHash12(cell + float2(frame * 71.13, frame * 13.77)) - 0.5;

    float luma = agslLuma(agslUnpremul(src).rgb);
    float bias = mix(1.0, 1.0 - abs(luma * 2.0 - 1.0), clamp(uMidtoneBias, 0.0, 1.0));

    half delta = half(noise * uIntensity * bias) * src.a;
    half3 result = half3(src.r + delta, src.g + delta, src.b + delta);
    result = clamp(result, half3(0.0), half3(src.a));
    return half4(result.r, result.g, result.b, src.a);
}
"""
)

/**
 * Плёночное зерно поверх контента.
 *
 * @param intensity амплитуда шума (0..1).
 * @param size размер зерна в пикселях; 1.0 — попиксельно.
 * @param midtoneBias насколько сильно прятать зерно в тенях и светах.
 * @param fps частота обновления зерна: 24 даёт «киношный» вид.
 */
@Composable
public fun Modifier.filmGrain(
    intensity: Float = 0.12f,
    size: Float = 1.5f,
    midtoneBias: Float = 0.6f,
    fps: Float = 24f,
    enabled: Boolean = true,
): Modifier = agslEffect(FilmGrainProgram, animated = enabled) {
    set("uIntensity", intensity)
    set("uSize", size)
    set("uMidtoneBias", midtoneBias)
    set("uFps", fps)
}
