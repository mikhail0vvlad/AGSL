package com.mikhailov.agslfx.core

import android.graphics.RuntimeShader
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density

/**
 * Область, в которой эффект проставляет свои униформы.
 *
 * Реализует [Density], поэтому внутри блока можно писать `12.dp.toPx()`.
 * Униформы `uResolution` и `uTime` библиотека проставляет сама до вызова блока.
 */
@Stable
public class AgslScope internal constructor(
    public val shader: RuntimeShader,
    public val size: Size,
    public val time: Float,
    density: Density,
) : Density by density {

    /** `uniform float` */
    public fun set(name: String, value: Float) {
        shader.setFloatUniform(name, value)
    }

    /** `uniform float2` */
    public fun set(name: String, x: Float, y: Float) {
        shader.setFloatUniform(name, x, y)
    }

    /** `uniform float3` */
    public fun set(name: String, x: Float, y: Float, z: Float) {
        shader.setFloatUniform(name, x, y, z)
    }

    /** `uniform float2` из [Offset]. */
    public fun set(name: String, value: Offset) {
        shader.setFloatUniform(name, value.x, value.y)
    }

    /** `uniform float2` из [Size]. */
    public fun set(name: String, value: Size) {
        shader.setFloatUniform(name, value.width, value.height)
    }

    /** `uniform int` */
    public fun set(name: String, value: Int) {
        shader.setIntUniform(name, value)
    }

    /**
     * `uniform half4` из [Color] в виде (r, g, b, a) в sRGB 0..1.
     *
     * Намеренно не используется `setColorUniform`: там подключается управление цветом,
     * из-за которого значения в шейдере перестают совпадать с тем, что видит разработчик.
     */
    public fun set(name: String, value: Color) {
        shader.setFloatUniform(name, value.red, value.green, value.blue, value.alpha)
    }

    /** Булев флаг как `uniform float` (0.0 / 1.0) — в AGSL нет bool-униформ. */
    public fun set(name: String, value: Boolean) {
        shader.setFloatUniform(name, if (value) 1f else 0f)
    }
}
