package com.mikhailov.agslfx.effect

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikhailov.agslfx.core.AgslProgram
import com.mikhailov.agslfx.core.agslEffect

/** Цифровой глитч: построчный сдвиг блоков плюс расхождение RGB-каналов. */
public val GlitchProgram: AgslProgram = AgslProgram(
    name = "Glitch",
    body = """
uniform shader content;
uniform float2 uResolution;
uniform float uTime;

uniform float uIntensity;  // 0 — выключено, 1 — полный хаос
uniform float uBlockSize;  // высота сбойной полосы в пикселях
uniform float uSplit;      // расхождение каналов в пикселях
uniform float uRate;       // сколько раз в секунду меняется картина сбоя

half4 main(float2 fragCoord) {
    float intensity = clamp(uIntensity, 0.0, 1.0);
    float frame = floor(uTime * max(uRate, 1.0));
    float row = floor(fragCoord.y / max(uBlockSize, 1.0));

    float rnd = agslHash12(float2(row, frame));
    float active = step(1.0 - intensity * 0.75, rnd);
    float jitter = (agslHash12(float2(row * 1.7 + 3.1, frame * 2.3)) - 0.5);
    float shift = jitter * uResolution.x * 0.3 * intensity * active;

    float2 sample = float2(fragCoord.x + shift, fragCoord.y);
    float split = uSplit * intensity * (0.35 + rnd);

    half red = content.eval(float2(sample.x + split, sample.y)).r;
    half4 green = content.eval(sample);
    half blue = content.eval(float2(sample.x - split, sample.y)).b;

    // Тонкая горизонтальная «развёртка», чтобы глитч не выглядел статичным.
    float sweep = fract(uTime * 0.37);
    float band = smoothstep(0.04, 0.0, abs(fragCoord.y / uResolution.y - sweep));
    half boost = half(1.0 + band * intensity * 0.8);

    return half4(red * boost, green.g, blue * boost, green.a);
}
"""
)

/**
 * Глитч-эффект.
 *
 * @param intensity общая сила сбоя.
 * @param blockSizePx высота сбойных полос.
 * @param channelSplitPx максимальное расхождение красного и синего каналов.
 * @param ratePerSecond как часто меняется рисунок сбоя.
 */
@Composable
public fun Modifier.glitch(
    intensity: Float = 0.5f,
    blockSizePx: Float = 24f,
    channelSplitPx: Float = 6f,
    ratePerSecond: Float = 12f,
    enabled: Boolean = true,
): Modifier = agslEffect(GlitchProgram, animated = enabled) {
    set("uIntensity", intensity)
    set("uBlockSize", blockSizePx)
    set("uSplit", channelSplitPx)
    set("uRate", ratePerSecond)
}
