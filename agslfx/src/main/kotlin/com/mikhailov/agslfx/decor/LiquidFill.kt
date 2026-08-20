package com.mikhailov.agslfx.decor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mikhailov.agslfx.core.AgslProgram
import com.mikhailov.agslfx.core.agslBackground

/** Заливка «жидкостью» с волной на поверхности — прогресс, который приятно смотреть. */
public val LiquidFillProgram: AgslProgram = AgslProgram(
    name = "Liquid fill",
    body = """
uniform float2 uResolution;
uniform float uTime;

uniform half4 uFill;
uniform half4 uTrack;
uniform half4 uFoam;
uniform float uProgress;   // 0..1
uniform float uAmplitude;  // высота волны в долях ширины
uniform float uWaves;      // число волн по вертикали
uniform float uVertical;   // 0 — заливка слева направо, 1 — снизу вверх

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / uResolution;

    // Ось заливки и ось, вдоль которой бежит волна.
    float along = mix(uv.x, 1.0 - uv.y, uVertical);
    float across = mix(uv.y, uv.x, uVertical);

    float wave = sin(across * uWaves * AGSL_TAU + uTime * 2.6) * uAmplitude
               + sin(across * uWaves * 1.7 * AGSL_TAU - uTime * 1.9) * uAmplitude * 0.45;

    float progress = clamp(uProgress, 0.0, 1.0);
    float edges = smoothstep(0.0, 0.04, progress) * smoothstep(1.0, 0.96, progress);
    float level = progress + wave * edges;

    float filled = smoothstep(level + 0.004, level - 0.004, along);
    float foam = smoothstep(0.035, 0.0, abs(along - level)) * edges;

    half3 col = mix(uTrack.rgb, uFill.rgb, half(filled));
    col = mix(col, uFoam.rgb, half(foam * uFoam.a));

    half alpha = mix(uTrack.a, uFill.a, half(filled));
    return half4(col.r * alpha, col.g * alpha, col.b * alpha, alpha);
}
"""
)

/**
 * Прогресс-заливка с волной. Работает как фон: положите поверх текст или иконку.
 *
 * @param progress 0..1.
 * @param vertical заливать снизу вверх вместо слева направо.
 * @param amplitude высота волны.
 */
@Composable
public fun Modifier.liquidFill(
    progress: Float,
    fill: Color = Color(0xFF2B7BFF),
    track: Color = Color(0xFF11162B),
    foam: Color = Color.White.copy(alpha = 0.65f),
    amplitude: Float = 0.03f,
    waves: Float = 1.6f,
    vertical: Boolean = false,
    speed: Float = 1f,
    enabled: Boolean = true,
): Modifier = agslBackground(LiquidFillProgram, animated = enabled, speed = speed) {
    set("uProgress", progress)
    set("uFill", fill)
    set("uTrack", track)
    set("uFoam", foam)
    set("uAmplitude", amplitude)
    set("uWaves", waves)
    set("uVertical", vertical)
}
