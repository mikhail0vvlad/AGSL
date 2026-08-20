package com.mikhailov.agslfx.effect

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mikhailov.agslfx.core.AgslProgram
import com.mikhailov.agslfx.core.agslEffect

/** Растворение по шумовой маске с раскалённой кромкой — «сгорающий» переход. */
public val DissolveProgram: AgslProgram = AgslProgram(
    name = "Dissolve",
    body = """
uniform shader content;
uniform float2 uResolution;

uniform float uProgress;   // 0 — контент целый, 1 — полностью растворён
uniform float uScale;      // масштаб шума
uniform float uEdge;       // ширина кромки
uniform half4 uEdgeColor;  // цвет кромки, a — сила свечения
uniform float uSeed;

half4 main(float2 fragCoord) {
    half4 src = content.eval(fragCoord);
    float2 uv = fragCoord / uResolution;

    float noise = agslFbm(uv * max(uScale, 0.5) + float2(uSeed, uSeed * 1.7));

    float edge = max(uEdge, 0.001);
    // Прогресс разворачиваем так, чтобы 0 и 1 были действительно «целое» и «пусто».
    float threshold = uProgress * (1.0 + edge * 3.0) - edge;

    float mask = smoothstep(threshold, threshold + edge, noise);
    float rim = (1.0 - mask) * smoothstep(threshold - edge, threshold, noise);

    half glow = half(rim * uEdgeColor.a) * src.a;
    half4 body = src * half(mask);

    return half4(
        body.r + uEdgeColor.r * glow,
        body.g + uEdgeColor.g * glow,
        body.b + uEdgeColor.b * glow,
        min(body.a + glow, src.a)
    );
}
"""
)

/**
 * Растворение контента по фрактальному шуму.
 *
 * @param progress 0 — контент виден целиком, 1 — исчез.
 * @param scale масштаб шумовых пятен.
 * @param edgeWidth ширина светящейся кромки.
 * @param edgeColor цвет кромки; альфа задаёт яркость свечения.
 * @param seed сдвиг шума — разным элементам стоит дать разный seed.
 */
@Composable
public fun Modifier.dissolve(
    progress: Float,
    scale: Float = 6f,
    edgeWidth: Float = 0.08f,
    edgeColor: Color = Color(0xFFFF7A18),
    seed: Float = 0f,
): Modifier = agslEffect(DissolveProgram, animated = false) {
    set("uProgress", progress)
    set("uScale", scale)
    set("uEdge", edgeWidth)
    set("uEdgeColor", edgeColor)
    set("uSeed", seed)
}
