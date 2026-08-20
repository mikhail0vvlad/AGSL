package com.mikhailov.agslfx.effect

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.mikhailov.agslfx.core.AgslProgram
import com.mikhailov.agslfx.core.agslEffect

/** Расходящаяся волна по воде: смещение сэмплов вдоль радиуса плюс блик на гребне. */
public val WaterRippleProgram: AgslProgram = AgslProgram(
    name = "Water ripple",
    body = """
uniform shader content;
uniform float2 uResolution;

uniform float2 uCenter;    // центр волны в пикселях
uniform float uProgress;   // 0..1 — насколько далеко ушёл фронт
uniform float uAmplitude;  // максимальное смещение в пикселях
uniform float uFrequency;  // частота ряби
uniform float uDecay;      // ширина «пакета» волн

half4 main(float2 fragCoord) {
    float2 delta = fragCoord - uCenter;
    float dist = length(delta);
    float maxRadius = length(uResolution);

    float front = uProgress * maxRadius;
    float relative = dist - front;

    float envelope = exp(-abs(relative) / max(maxRadius * uDecay, 1.0)) * (1.0 - uProgress);
    float wave = sin(relative * uFrequency * 0.05 - uProgress * 18.0);

    float2 dir = dist > 0.001 ? delta / dist : float2(0.0, 0.0);
    float2 offset = dir * (wave * envelope * uAmplitude);

    half4 c = content.eval(fragCoord + offset);
    half spec = half(wave * envelope * 0.18) * c.a;
    return half4(c.r + spec, c.g + spec, c.b + spec, c.a);
}
"""
)

/**
 * Волна-рябь из точки [center].
 *
 * @param center центр волны в пикселях относительно композабла.
 * @param progress прогресс расхождения волны 0..1 (обычно анимируется `Animatable`).
 * @param amplitudePx максимальное смещение пикселей.
 * @param frequency частота ряби.
 * @param decay ширина волнового пакета в долях диагонали.
 */
@Composable
public fun Modifier.waterRipple(
    center: Offset,
    progress: Float,
    amplitudePx: Float = 28f,
    frequency: Float = 12f,
    decay: Float = 0.14f,
): Modifier = agslEffect(WaterRippleProgram, animated = false) {
    set("uCenter", center)
    set("uProgress", progress)
    set("uAmplitude", amplitudePx)
    set("uFrequency", frequency)
    set("uDecay", decay)
}
