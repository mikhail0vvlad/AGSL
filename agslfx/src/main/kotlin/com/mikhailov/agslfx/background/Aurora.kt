package com.mikhailov.agslfx.background

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mikhailov.agslfx.core.AgslProgram
import com.mikhailov.agslfx.core.agslBackground

/** Северное сияние: две ленты фрактального шума, подсвеченные тремя цветами. */
public val AuroraProgram: AgslProgram = AgslProgram(
    name = "Aurora",
    body = """
uniform float2 uResolution;
uniform float uTime;

uniform half4 uBackground;
uniform half4 uColorA;
uniform half4 uColorB;
uniform half4 uColorC;
uniform float uIntensity;
uniform float uScale;

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / uResolution;
    float aspect = uResolution.x / max(uResolution.y, 1.0);
    float2 p = float2(uv.x * aspect, uv.y) * uScale;

    float t = uTime * 0.15;
    float n1 = agslFbm(p + float2(t, -t * 0.6));
    float n2 = agslFbm(p * 1.5 + float2(-t * 0.8, t * 0.45) + float2(n1, n1));

    // Ленты тем ярче, чем выше по экрану — как настоящее сияние.
    float sky = smoothstep(1.0, 0.05, uv.y);
    float band1 = smoothstep(0.30, 0.72, n1) * sky;
    float band2 = smoothstep(0.35, 0.85, n2) * sky;
    float core = pow(band1 * band2, 2.0);

    half3 col = uBackground.rgb;
    col = mix(col, uColorA.rgb, half(band1 * uIntensity));
    col = mix(col, uColorB.rgb, half(band2 * 0.75 * uIntensity));
    col = mix(col, uColorC.rgb, half(core * uIntensity));

    // Лёгкое зерно, чтобы не было полос на градиенте.
    float dither = (agslHash12(fragCoord) - 0.5) * 0.012;
    col += half3(half(dither));

    return half4(col.r, col.g, col.b, 1.0);
}
"""
)

/**
 * Живой фон «северное сияние».
 *
 * @param background базовый цвет неба.
 * @param scale масштаб лент.
 * @param intensity общая насыщенность.
 */
@Composable
public fun Modifier.auroraBackground(
    background: Color = Color(0xFF060A18),
    colorA: Color = Color(0xFF1B7A6B),
    colorB: Color = Color(0xFF3E5BD6),
    colorC: Color = Color(0xFFB86BE8),
    intensity: Float = 0.9f,
    scale: Float = 2.2f,
    speed: Float = 1f,
    enabled: Boolean = true,
): Modifier = agslBackground(AuroraProgram, animated = enabled, speed = speed) {
    set("uBackground", background)
    set("uColorA", colorA)
    set("uColorB", colorB)
    set("uColorC", colorC)
    set("uIntensity", intensity)
    set("uScale", scale)
}
