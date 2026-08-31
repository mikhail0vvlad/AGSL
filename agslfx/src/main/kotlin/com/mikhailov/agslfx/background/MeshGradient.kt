package com.mikhailov.agslfx.background

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mikhailov.agslfx.core.AgslProgram
import com.mikhailov.agslfx.core.agslBackground

/**
 * Mesh-градиент: четыре цветные точки плавают по площади,
 * цвет пикселя — взвешенное по обратному расстоянию среднее.
 */
public val MeshGradientProgram: AgslProgram = AgslProgram(
    name = "Mesh gradient",
    body = """
uniform float2 uResolution;
uniform float uTime;

uniform half4 uColor0;
uniform half4 uColor1;
uniform half4 uColor2;
uniform half4 uColor3;
uniform float uFalloff;  // резкость переходов
uniform float uSpread;   // амплитуда блуждания точек

half4 main(float2 fragCoord) {
    float aspect = uResolution.x / max(uResolution.y, 1.0);
    float2 uv = float2(fragCoord.x / uResolution.x * aspect, fragCoord.y / uResolution.y);

    float t = uTime;
    float s = uSpread;

    float2 p0 = float2((0.5 + s * sin(t * 0.63)) * aspect, 0.5 + s * cos(t * 0.81));
    float2 p1 = float2((0.5 + s * sin(t * 0.47 + 2.1)) * aspect, 0.5 + s * cos(t * 0.55 + 1.3));
    float2 p2 = float2((0.5 + s * cos(t * 0.71 + 4.2)) * aspect, 0.5 + s * sin(t * 0.39 + 3.7));
    float2 p3 = float2((0.5 + s * cos(t * 0.33 + 1.1)) * aspect, 0.5 + s * sin(t * 0.67 + 5.2));

    float falloff = max(uFalloff, 0.5);
    float w0 = 1.0 / (pow(distance(uv, p0), falloff) + 0.0008);
    float w1 = 1.0 / (pow(distance(uv, p1), falloff) + 0.0008);
    float w2 = 1.0 / (pow(distance(uv, p2), falloff) + 0.0008);
    float w3 = 1.0 / (pow(distance(uv, p3), falloff) + 0.0008);
    float total = w0 + w1 + w2 + w3;

    half3 col = (uColor0.rgb * half(w0) + uColor1.rgb * half(w1)
               + uColor2.rgb * half(w2) + uColor3.rgb * half(w3)) / half(total);

    float dither = (agslHash12(fragCoord) - 0.5) * 0.012;
    col += half3(half(dither));

    return half4(col.r, col.g, col.b, 1.0);
}
"""
)

/**
 * Анимированный mesh-градиент — тот самый «жидкий» фон из современных лендингов.
 *
 * @param falloff резкость перехода между цветами (1.5..4).
 * @param spread насколько далеко разбегаются цветовые точки.
 */
@Composable
public fun Modifier.meshGradient(
    color0: Color = Color(0xFFFFCC00),
    color1: Color = Color(0xFFFC3F1D),
    color2: Color = Color(0xFFFF8A00),
    color3: Color = Color(0xFFB32000),
    falloff: Float = 2.2f,
    spread: Float = 0.32f,
    speed: Float = 0.35f,
    enabled: Boolean = true,
): Modifier = agslBackground(MeshGradientProgram, animated = enabled, speed = speed) {
    set("uColor0", color0)
    set("uColor1", color1)
    set("uColor2", color2)
    set("uColor3", color3)
    set("uFalloff", falloff)
    set("uSpread", spread)
}
