package com.mikhailov.agslfx.decor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mikhailov.agslfx.core.AgslProgram
import com.mikhailov.agslfx.core.agslForeground

/** Вращающаяся коническая обводка по контуру скруглённого прямоугольника. */
public val AnimatedBorderProgram: AgslProgram = AgslProgram(
    name = "Animated border",
    body = """
uniform float2 uResolution;
uniform float uTime;

uniform half4 uColor0;
uniform half4 uColor1;
uniform half4 uColor2;
uniform float uWidth;        // толщина обводки в пикселях
uniform float uCornerRadius;
uniform float uGlow;         // мягкое свечение наружу

half4 main(float2 fragCoord) {
    float2 halfSize = uResolution * 0.5;
    float2 p = fragCoord - halfSize;

    float radius = min(uCornerRadius, min(halfSize.x, halfSize.y));
    float dist = agslSdRoundRect(p, halfSize - float2(uWidth, uWidth) * 0.5, radius);

    float width = max(uWidth, 1.0);
    float stroke = 1.0 - smoothstep(0.0, width * 0.5, abs(dist));
    float glow = (1.0 - smoothstep(0.0, width * 3.0, abs(dist))) * uGlow;

    float angle = atan(p.y, p.x) + uTime;
    float h = fract(angle / AGSL_TAU);

    half3 col;
    if (h < 0.3333) {
        col = mix(uColor0.rgb, uColor1.rgb, half(h * 3.0));
    } else if (h < 0.6666) {
        col = mix(uColor1.rgb, uColor2.rgb, half((h - 0.3333) * 3.0));
    } else {
        col = mix(uColor2.rgb, uColor0.rgb, half((h - 0.6666) * 3.0));
    }

    half alpha = half(clamp(stroke + glow * 0.45, 0.0, 1.0));
    return half4(col.r * alpha, col.g * alpha, col.b * alpha, alpha);
}
"""
)

/**
 * Живая градиентная обводка поверх контента.
 *
 * @param width толщина обводки.
 * @param cornerRadius радиус скругления.
 * @param glow мягкое свечение вокруг линии.
 */
@Composable
public fun Modifier.animatedBorder(
    color0: Color = Color(0xFFFFCC00),
    color1: Color = Color(0xFFFC3F1D),
    color2: Color = Color(0xFFFFFFFF),
    width: Dp = 2.dp,
    cornerRadius: Dp = 20.dp,
    glow: Float = 0.6f,
    speed: Float = 1f,
    enabled: Boolean = true,
): Modifier = agslForeground(AnimatedBorderProgram, animated = enabled, speed = speed) {
    set("uColor0", color0)
    set("uColor1", color1)
    set("uColor2", color2)
    set("uWidth", width.toPx())
    set("uCornerRadius", cornerRadius.toPx())
    set("uGlow", glow)
}
