package com.mikhailov.agslfx.effect

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mikhailov.agslfx.core.AgslProgram
import com.mikhailov.agslfx.core.agslEffect

/**
 * Матовое стекло: рассеивание по золотому спиральному ядру (16 отсчётов),
 * джиттер по хешу вместо регулярной сетки — иначе видны «звёздочки».
 */
public val FrostedGlassProgram: AgslProgram = AgslProgram(
    name = "Frosted glass",
    body = """
uniform shader content;
uniform float uTime;

uniform float uRadius;   // радиус рассеивания в пикселях
uniform half4 uTint;     // подкраска, a — сила
uniform float uNoise;    // матовость (зерно на стекле)

const int AGSL_TAPS = 16;

half4 main(float2 fragCoord) {
    float jitter = agslHash12(fragCoord) * AGSL_TAU;
    half4 sum = half4(0.0);

    for (int i = 0; i < AGSL_TAPS; i++) {
        float fi = float(i);
        float angle = jitter + fi * 2.39996323;                 // золотой угол
        float radius = sqrt((fi + 0.5) / float(AGSL_TAPS)) * uRadius;
        sum += content.eval(fragCoord + float2(cos(angle), sin(angle)) * radius);
    }

    half4 blurred = sum / half(AGSL_TAPS);

    float grain = (agslHash12(fragCoord * 1.7 + float2(uTime, uTime)) - 0.5) * uNoise;
    half3 tinted = mix(blurred.rgb, uTint.rgb * blurred.a, half(uTint.a));
    tinted += half3(half(grain)) * blurred.a;

    return half4(tinted.r, tinted.g, tinted.b, blurred.a);
}
"""
)

/**
 * Стеклянная линза: преломление у краёв по SDF скруглённого прямоугольника
 * плюс зеркальный блик на фаске. Тот самый «liquid glass».
 */
public val LiquidGlassProgram: AgslProgram = AgslProgram(
    name = "Liquid glass",
    body = """
uniform shader content;
uniform float2 uResolution;

uniform float uCornerRadius; // радиус скругления в пикселях
uniform float uThickness;    // ширина фаски в пикселях
uniform float uRefraction;   // сила преломления в пикселях
uniform float uGlare;        // яркость блика
uniform half4 uTint;         // подкраска стекла

half4 main(float2 fragCoord) {
    float2 halfSize = uResolution * 0.5;
    float2 p = fragCoord - halfSize;

    float radius = min(uCornerRadius, min(halfSize.x, halfSize.y));
    float dist = agslSdRoundRect(p, halfSize, radius);

    // Нормаль поверхности — градиент поля расстояний.
    float e = 1.0;
    float dx = agslSdRoundRect(p + float2(e, 0.0), halfSize, radius)
             - agslSdRoundRect(p - float2(e, 0.0), halfSize, radius);
    float dy = agslSdRoundRect(p + float2(0.0, e), halfSize, radius)
             - agslSdRoundRect(p - float2(0.0, e), halfSize, radius);
    float2 gradient = normalize(float2(dx, dy) + float2(0.0001, 0.0001));

    // 1 у самого края, 0 в глубине стекла.
    float bevel = 1.0 - clamp(-dist / max(uThickness, 1.0), 0.0, 1.0);
    float2 offset = gradient * (bevel * bevel * uRefraction);

    half4 c = content.eval(fragCoord + offset);

    float2 lightDir = normalize(float2(-0.6, -0.8));
    float spec = pow(clamp(dot(gradient, lightDir), 0.0, 1.0), 6.0) * bevel * uGlare;

    half3 col = mix(c.rgb, uTint.rgb * c.a, half(uTint.a));
    col += half3(half(spec)) * c.a;

    float inside = 1.0 - smoothstep(-1.0, 1.0, dist);
    return half4(col.r, col.g, col.b, c.a) * half(inside);
}
"""
)

/**
 * Матовое стекло поверх собственного контента.
 *
 * @param radius радиус рассеивания.
 * @param tint подкраска стекла (альфа — сила).
 * @param noise матовость поверхности.
 */
@Composable
public fun Modifier.frostedGlass(
    radius: Dp = 16.dp,
    tint: Color = Color.White.copy(alpha = 0.12f),
    noise: Float = 0.03f,
): Modifier {
    return agslEffect(FrostedGlassProgram, animated = noise > 0f) {
        set("uRadius", radius.toPx())
        set("uTint", tint)
        set("uNoise", noise)
    }
}

/**
 * Стеклянная линза со скруглёнными углами.
 *
 * @param cornerRadius радиус скругления «стекла».
 * @param thickness ширина фаски, на которой происходит преломление.
 * @param refraction сила преломления в пикселях.
 * @param glare яркость зеркального блика.
 * @param tint подкраска стекла.
 */
@Composable
public fun Modifier.liquidGlass(
    cornerRadius: Dp = 28.dp,
    thickness: Dp = 24.dp,
    refraction: Float = 18f,
    glare: Float = 0.8f,
    tint: Color = Color.White.copy(alpha = 0.06f),
): Modifier = agslEffect(LiquidGlassProgram, animated = false) {
    set("uCornerRadius", cornerRadius.toPx())
    set("uThickness", thickness.toPx())
    set("uRefraction", refraction)
    set("uGlare", glare)
    set("uTint", tint)
}
