package com.mikhailov.agslfx.background

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mikhailov.agslfx.core.AgslProgram
import com.mikhailov.agslfx.core.agslBackground

/** Три параллаксных слоя мерцающих звёзд на хеш-сетке. */
public val StarfieldProgram: AgslProgram = AgslProgram(
    name = "Starfield",
    body = """
uniform float2 uResolution;
uniform float uTime;

uniform half4 uBackground;
uniform half4 uStarColor;
uniform float uDensity;   // сколько ячеек на высоту экрана
uniform float uDrift;     // скорость параллакса
uniform float uTwinkle;

const int AGSL_LAYERS = 3;

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / max(uResolution.y, 1.0);
    half3 col = uBackground.rgb;

    for (int layer = 0; layer < AGSL_LAYERS; layer++) {
        float fl = float(layer);
        float scale = max(uDensity, 2.0) * (1.0 + fl * 0.75);
        float2 grid = uv * scale + float2(uTime * uDrift * (0.15 + fl * 0.12), fl * 11.3);

        float2 id = floor(grid);
        float2 local = fract(grid) - float2(0.5, 0.5);
        float2 rnd = agslHash22(id + float2(fl * 37.0, fl * 17.0));

        float2 jitter = (rnd - float2(0.5, 0.5)) * 0.72;
        float dist = length(local - jitter);

        float phase = uTime * (1.0 + rnd.x * 3.0) + rnd.y * AGSL_TAU;
        float twinkle = mix(1.0, 0.35 + 0.65 * (0.5 + 0.5 * sin(phase)), uTwinkle);

        float size = mix(0.03, 0.075, rnd.x) / (1.0 + fl * 0.5);
        float star = smoothstep(size, 0.0, dist) * twinkle;

        // Крестообразный блик у самых крупных звёзд.
        float flare = smoothstep(0.5, 0.0, abs(local.x - jitter.x) * 22.0)
                    * smoothstep(0.35, 0.0, abs(local.y - jitter.y) * 3.0)
                    * step(0.85, rnd.x);

        col += uStarColor.rgb * half((star + flare * 0.35) * (1.0 - fl * 0.22));
    }

    return half4(col.r, col.g, col.b, 1.0);
}
"""
)

/**
 * Звёздное небо с параллаксом и мерцанием.
 *
 * @param density количество ячеек сетки по высоте экрана.
 * @param drift скорость горизонтального дрейфа.
 * @param twinkle 0 — ровный свет, 1 — активное мерцание.
 */
@Composable
public fun Modifier.starfield(
    background: Color = Color(0xFF04040C),
    starColor: Color = Color(0xFFEAF2FF),
    density: Float = 14f,
    drift: Float = 0.35f,
    twinkle: Float = 0.9f,
    speed: Float = 1f,
    enabled: Boolean = true,
): Modifier = agslBackground(StarfieldProgram, animated = enabled, speed = speed) {
    set("uBackground", background)
    set("uStarColor", starColor)
    set("uDensity", density)
    set("uDrift", drift)
    set("uTwinkle", twinkle)
}
