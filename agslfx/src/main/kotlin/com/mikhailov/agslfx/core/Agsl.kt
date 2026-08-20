package com.mikhailov.agslfx.core

import android.graphics.RuntimeShader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember

/**
 * Общие имена униформ, которые ядро библиотеки проставляет автоматически.
 */
public object AgslUniform {
    /** `uniform shader content` — исходный контент композабла (только для эффектов над контентом). */
    public const val CONTENT: String = "content"

    /** `uniform float2 uResolution` — размер области отрисовки в пикселях. */
    public const val RESOLUTION: String = "uResolution"

    /** `uniform float uTime` — время в секундах с момента запуска эффекта. */
    public const val TIME: String = "uTime"
}

/**
 * AGSL-программа: тело шейдера + человекочитаемое имя.
 *
 * К телу автоматически подклеивается [Agsl.PRELUDE] — небольшая «стандартная библиотека»
 * (хеши, value-noise, fbm, SDF прямоугольника со скруглением и т.п.).
 *
 * Хранить программу как объект удобно тем, что демо-приложение может показать
 * ровно тот же исходник, который реально компилируется на устройстве.
 */
@Immutable
public data class AgslProgram(
    val name: String,
    val body: String,
    val usesPrelude: Boolean = true,
) {
    /** Полный исходник, который уходит в [RuntimeShader]. */
    public val source: String
        get() = if (usesPrelude) Agsl.PRELUDE + body else body

    /** Создаёт новый экземпляр рантайм-шейдера. Бросает исключение, если AGSL не компилируется. */
    public fun create(): RuntimeShader = RuntimeShader(source)

    /**
     * Имена униформ, объявленных в теле программы.
     *
     * RuntimeShader.setFloatUniform бросает исключение для неизвестного имени,
     * поэтому ядро сверяется с этим списком, прежде чем автоматически проставлять
     * uResolution и uTime — конкретному шейдеру они могут быть не нужны.
     */
    public val declaredUniforms: Set<String> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        UNIFORM_REGEX.findAll(body).map { it.groupValues[1] }.toSet()
    }

    private companion object {
        private val UNIFORM_REGEX = Regex("\\buniform\\s+\\w+\\s+(\\w+)\\s*;")
    }
}

public object Agsl {

    /**
     * Мини-стдлиб для AGSL.
     *
     * Важное ограничение AGSL (SkSL, профиль ES2): циклы должны иметь границы,
     * известные на этапе компиляции, поэтому число октав/слоёв здесь зашито константами.
     */
    public const val PRELUDE: String = """
const float AGSL_PI = 3.1415926535;
const float AGSL_TAU = 6.2831853071;

float agslHash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

float agslHash12(float2 p) {
    float3 p3 = fract(float3(p.x, p.y, p.x) * 0.1031);
    p3 += dot(p3, float3(p3.y, p3.z, p3.x) + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float2 agslHash22(float2 p) {
    float3 p3 = fract(float3(p.x, p.y, p.x) * float3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, float3(p3.y, p3.z, p3.x) + 33.33);
    return fract(float2(p3.x + p3.y, p3.x + p3.z) * float2(p3.z, p3.y));
}

float agslNoise(float2 p) {
    float2 i = floor(p);
    float2 f = fract(p);
    float2 u = f * f * (3.0 - 2.0 * f);
    float a = agslHash12(i);
    float b = agslHash12(i + float2(1.0, 0.0));
    float c = agslHash12(i + float2(0.0, 1.0));
    float d = agslHash12(i + float2(1.0, 1.0));
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float agslFbm(float2 p) {
    float2 q = p;
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 5; i++) {
        value += amplitude * agslNoise(q);
        q = q * 2.02 + float2(37.0, 17.0);
        amplitude *= 0.5;
    }
    return value;
}

float2 agslRotate(float2 p, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return float2(c * p.x - s * p.y, s * p.x + c * p.y);
}

float agslLuma(half3 c) {
    return dot(float3(c.r, c.g, c.b), float3(0.2126, 0.7152, 0.0722));
}

// Знаковое расстояние до прямоугольника со скруглёнными углами.
// p — координата относительно центра, b — половина размера, r — радиус скругления.
float agslSdRoundRect(float2 p, float2 b, float r) {
    float2 q = abs(p) - b + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

// Цвета в AGSL premultiplied. Эти два хелпера дают удобно поработать в «прямом» пространстве.
half4 agslUnpremul(half4 c) {
    return c.a > 0.0 ? half4(c.r / c.a, c.g / c.a, c.b / c.a, c.a) : c;
}

half4 agslPremul(half4 c) {
    return half4(c.r * c.a, c.g * c.a, c.b * c.a, c.a);
}
"""

    /** Компилирует и кеширует шейдер на время жизни композиции. */
    @Composable
    public fun rememberShader(program: AgslProgram): RuntimeShader =
        remember(program) { program.create() }
}
