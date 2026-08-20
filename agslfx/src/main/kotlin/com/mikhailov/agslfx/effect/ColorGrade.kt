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

/** Полиграфический растр: точки переменного радиуса на повёрнутой сетке. */
public val HalftoneProgram: AgslProgram = AgslProgram(
    name = "Halftone",
    body = """
uniform shader content;

uniform float uCellSize;  // шаг растровой сетки в пикселях
uniform float uAngle;     // поворот сетки
uniform half4 uInk;
uniform half4 uPaper;
uniform float uAmount;

half4 main(float2 fragCoord) {
    half4 src = content.eval(fragCoord);
    half4 straight = agslUnpremul(src);
    float luma = agslLuma(straight.rgb);

    float2 gridPoint = agslRotate(fragCoord, uAngle) / max(uCellSize, 2.0);
    float2 cell = fract(gridPoint) - float2(0.5, 0.5);
    float dist = length(cell);

    float radius = sqrt(clamp(1.0 - luma, 0.0, 1.0)) * 0.72;
    float coverage = smoothstep(radius, radius - 0.09, dist);

    half3 printed = mix(uPaper.rgb, uInk.rgb, half(coverage));
    half3 result = mix(straight.rgb, printed, half(clamp(uAmount, 0.0, 1.0)));

    return half4(result.r * src.a, result.g * src.a, result.b * src.a, src.a);
}
"""
)

/** Мозаика: квантование координат плюс зазор между «пикселями». */
public val PixelateProgram: AgslProgram = AgslProgram(
    name = "Pixelate",
    body = """
uniform shader content;

uniform float uSize;  // размер пикселя
uniform float uGap;   // зазор между пикселями 0..1

half4 main(float2 fragCoord) {
    float size = max(uSize, 1.0);
    float2 snapped = (floor(fragCoord / size) + float2(0.5, 0.5)) * size;
    half4 c = content.eval(snapped);

    float2 local = abs(fract(fragCoord / size) - float2(0.5, 0.5)) * 2.0;
    float edge = max(local.x, local.y);
    float mask = 1.0 - smoothstep(1.0 - clamp(uGap, 0.0, 0.9), 1.0, edge);

    return c * half(mask);
}
"""
)

/** ЭЛТ-монитор: бочка, расхождение каналов, строчная развёртка и виньетка. */
public val CrtProgram: AgslProgram = AgslProgram(
    name = "CRT",
    body = """
uniform shader content;
uniform float2 uResolution;
uniform float uTime;

uniform float uCurvature;   // кривизна экрана
uniform float uScanline;    // сила строк
uniform float uDensity;     // плотность строк
uniform float uVignette;
uniform float uAberration;  // расхождение каналов в пикселях
uniform float uFlicker;

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / uResolution;
    float2 centered = uv * 2.0 - 1.0;
    float r2 = dot(centered, centered);

    float2 warped = centered * (1.0 + uCurvature * r2 * 0.25);
    float2 warpedUv = warped * 0.5 + 0.5;

    if (warpedUv.x < 0.0 || warpedUv.x > 1.0 || warpedUv.y < 0.0 || warpedUv.y > 1.0) {
        return half4(0.0, 0.0, 0.0, 1.0);
    }

    float2 samplePoint = warpedUv * uResolution;
    float aberration = uAberration * (0.35 + r2);

    half red = content.eval(samplePoint + float2(aberration, 0.0)).r;
    half4 green = content.eval(samplePoint);
    half blue = content.eval(samplePoint - float2(aberration, 0.0)).b;

    float scan = 1.0 - uScanline * (0.5 + 0.5 * sin(samplePoint.y * uDensity));
    float vignette = 1.0 - uVignette * r2 * 0.55;
    float flicker = 1.0 - uFlicker * 0.5 * (0.5 + 0.5 * sin(uTime * 47.0));

    half gain = half(clamp(scan * vignette * flicker, 0.0, 1.0));
    return half4(red * gain, green.g * gain, blue * gain, green.a);
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

/**
 * Полиграфический растр.
 *
 * @param cellSizePx шаг сетки точек.
 * @param angleRadians поворот сетки (классика — 0.4 рад).
 */
@Composable
public fun Modifier.halftone(
    cellSizePx: Float = 10f,
    angleRadians: Float = 0.4f,
    ink: Color = Color(0xFF10121A),
    paper: Color = Color(0xFFF3F0E7),
    amount: Float = 1f,
): Modifier = agslEffect(HalftoneProgram, animated = false) {
    set("uCellSize", cellSizePx)
    set("uAngle", angleRadians)
    set("uInk", ink)
    set("uPaper", paper)
    set("uAmount", amount)
}

/**
 * Мозаика/пикселизация.
 *
 * @param sizePx размер «пикселя».
 * @param gap зазор между пикселями, 0 — сплошная мозаика.
 */
@Composable
public fun Modifier.pixelate(
    sizePx: Float = 14f,
    gap: Float = 0.12f,
): Modifier = agslEffect(PixelateProgram, animated = false) {
    set("uSize", sizePx)
    set("uGap", gap)
}

/**
 * ЭЛТ-экран.
 *
 * @param curvature кривизна «кинескопа».
 * @param scanline сила строчной развёртки.
 * @param density плотность строк (в радианах на пиксель).
 */
@Composable
public fun Modifier.crt(
    curvature: Float = 0.35f,
    scanline: Float = 0.35f,
    density: Float = 1.6f,
    vignette: Float = 0.5f,
    aberrationPx: Float = 1.6f,
    flicker: Float = 0.06f,
    enabled: Boolean = true,
): Modifier = agslEffect(CrtProgram, animated = enabled) {
    set("uCurvature", curvature)
    set("uScanline", scanline)
    set("uDensity", density)
    set("uVignette", vignette)
    set("uAberration", aberrationPx)
    set("uFlicker", flicker)
}
