package com.mikhailov.agslfx.effect

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mikhailov.agslfx.core.AgslProgram
import com.mikhailov.agslfx.core.agslEffect

/** Дуотон: яркость контента раскладывается между двумя цветами. */
public val DuotoneProgram: AgslProgram = AgslProgram(
    name = "Duotone",
    body = """
uniform shader content;

uniform half4 uShadow;
uniform half4 uHighlight;
uniform float uAmount;    // 0 — оригинал, 1 — полный дуотон
uniform float uContrast;

half4 main(float2 fragCoord) {
    half4 src = content.eval(fragCoord);
    half4 straight = agslUnpremul(src);

    float luma = agslLuma(straight.rgb);
    luma = clamp((luma - 0.5) * uContrast + 0.5, 0.0, 1.0);

    half3 duo = mix(uShadow.rgb, uHighlight.rgb, half(luma));
    half3 result = mix(straight.rgb, duo, half(clamp(uAmount, 0.0, 1.0)));

    return half4(result.r * src.a, result.g * src.a, result.b * src.a, src.a);
}
"""
)

/**
 * Дуотон.
 *
 * @param shadow цвет теней, @param highlight цвет светов.
 * @param amount степень подмены оригинального цвета.
 * @param contrast контраст яркостной маски.
 */
@Composable
public fun Modifier.duotone(
    shadow: Color = Color(0xFF0B1026),
    highlight: Color = Color(0xFF66E0FF),
    amount: Float = 1f,
    contrast: Float = 1.15f,
): Modifier = agslEffect(DuotoneProgram, animated = false) {
    set("uShadow", shadow)
    set("uHighlight", highlight)
    set("uAmount", amount)
    set("uContrast", contrast)
}
