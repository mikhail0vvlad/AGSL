package com.mikhailov.agslfx.effect

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mikhailov.agslfx.core.AgslProgram
import com.mikhailov.agslfx.core.agslEffect

/** Диагональный блик, бегущий по контенту. Классический skeleton-loading. */
public val ShimmerProgram: AgslProgram = AgslProgram(
    name = "Shimmer",
    body = """
uniform shader content;
uniform float2 uResolution;
uniform float uTime;

uniform half4 uHighlight;  // цвет блика, a — сила
uniform float uWidth;      // ширина полосы в долях диагонали
uniform float uAngle;      // наклон полосы в радианах
uniform float uSpeed;      // проходов в секунду

half4 main(float2 fragCoord) {
    half4 src = content.eval(fragCoord);
    float2 uv = fragCoord / uResolution;

    float2 dir = float2(cos(uAngle), sin(uAngle));
    float projection = dot(uv - float2(0.5, 0.5), dir) + 0.5;

    float width = max(uWidth, 0.001);
    float travel = fract(uTime * uSpeed);
    float head = travel * (1.0 + width * 2.0) - width;

    float band = smoothstep(width, 0.0, abs(projection - head));
    band *= band;

    // Контент премультиплицирован, поэтому блик тоже домножаем на альфу источника.
    half3 sheen = uHighlight.rgb * half(band * uHighlight.a);
    return half4(src.r + sheen.r * src.a,
                 src.g + sheen.g * src.a,
                 src.b + sheen.b * src.a,
                 src.a);
}
"""
)

/**
 * Бегущий блик поверх контента.
 *
 * @param highlight цвет блика, альфа задаёт силу.
 * @param width ширина полосы в долях размера (0.05..0.6 выглядит разумно).
 * @param angleRadians наклон полосы.
 * @param speed число проходов в секунду.
 */
@Composable
public fun Modifier.shimmer(
    highlight: Color = Color.White.copy(alpha = 0.55f),
    width: Float = 0.18f,
    angleRadians: Float = 0.6f,
    speed: Float = 0.6f,
    enabled: Boolean = true,
): Modifier = agslEffect(ShimmerProgram, animated = enabled) {
    set("uHighlight", highlight)
    set("uWidth", width)
    set("uAngle", angleRadians)
    set("uSpeed", speed)
}
