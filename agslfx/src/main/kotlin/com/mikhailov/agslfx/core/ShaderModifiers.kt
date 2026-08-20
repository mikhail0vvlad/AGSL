package com.mikhailov.agslfx.core

import android.graphics.RenderEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Применяет AGSL-программу как [RenderEffect] к содержимому композабла.
 *
 * Шейдер обязан объявлять `uniform shader content` — через него сэмплится исходная
 * отрисовка. Униформы `uResolution` (px) и `uTime` (сек) проставляются автоматически.
 *
 * Блок [uniforms] выполняется в контексте слоя: чтение анимированных состояний внутри него
 * приводит к перерисовке слоя, но не к рекомпозиции.
 *
 * @param program AGSL-программа.
 * @param animated запускать ли кадровые часы `uTime`.
 * @param speed множитель скорости времени.
 * @param clipToBounds обрезать ли результат по границам композабла.
 */
@Composable
public fun Modifier.agslEffect(
    program: AgslProgram,
    animated: Boolean = true,
    speed: Float = 1f,
    clipToBounds: Boolean = true,
    uniforms: AgslScope.() -> Unit = {},
): Modifier {
    val shader = Agsl.rememberShader(program)
    val time = rememberShaderTime(animated, speed)
    return this.graphicsLayer {
        val now = time.value
        val width = if (size.width > 0f) size.width else 1f
        val height = if (size.height > 0f) size.height else 1f
        if (AgslUniform.RESOLUTION in program.declaredUniforms) {
            shader.setFloatUniform(AgslUniform.RESOLUTION, width, height)
        }
        if (AgslUniform.TIME in program.declaredUniforms) {
            shader.setFloatUniform(AgslUniform.TIME, now)
        }
        AgslScope(shader, size, now, this).uniforms()
        renderEffect = RenderEffect
            .createRuntimeShaderEffect(shader, AgslUniform.CONTENT)
            .asComposeRenderEffect()
        clip = clipToBounds
    }
}

/**
 * Рисует генеративную AGSL-программу **под** содержимым композабла.
 * Шейдер не сэмплит контент — только пишет цвет, поэтому `uniform shader content` не нужен.
 */
@Composable
public fun Modifier.agslBackground(
    program: AgslProgram,
    animated: Boolean = true,
    speed: Float = 1f,
    uniforms: AgslScope.() -> Unit = {},
): Modifier = agslPaint(program, animated, speed, above = false, uniforms = uniforms)

/** То же самое, но поверх содержимого — удобно для рамок, бликов и оверлеев. */
@Composable
public fun Modifier.agslForeground(
    program: AgslProgram,
    animated: Boolean = true,
    speed: Float = 1f,
    uniforms: AgslScope.() -> Unit = {},
): Modifier = agslPaint(program, animated, speed, above = true, uniforms = uniforms)

@Composable
private fun Modifier.agslPaint(
    program: AgslProgram,
    animated: Boolean,
    speed: Float,
    above: Boolean,
    uniforms: AgslScope.() -> Unit,
): Modifier {
    val shader = Agsl.rememberShader(program)
    val time = rememberShaderTime(animated, speed)
    return this.drawWithCache {
        val brush = ShaderBrush(shader)
        onDrawWithContent {
            if (above) drawContent()
            val now = time.value
            val width = if (size.width > 0f) size.width else 1f
            val height = if (size.height > 0f) size.height else 1f
            if (AgslUniform.RESOLUTION in program.declaredUniforms) {
                shader.setFloatUniform(AgslUniform.RESOLUTION, width, height)
            }
            if (AgslUniform.TIME in program.declaredUniforms) {
                shader.setFloatUniform(AgslUniform.TIME, now)
            }
            AgslScope(shader, size, now, this).uniforms()
            drawRect(brush)
            if (!above) drawContent()
        }
    }
}
