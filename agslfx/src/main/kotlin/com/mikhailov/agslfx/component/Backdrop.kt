package com.mikhailov.agslfx.component

import android.graphics.RenderEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mikhailov.agslfx.core.Agsl
import com.mikhailov.agslfx.core.AgslProgram
import com.mikhailov.agslfx.core.AgslScope
import com.mikhailov.agslfx.core.AgslUniform
import com.mikhailov.agslfx.core.rememberShaderTime
import com.mikhailov.agslfx.effect.FrostedGlassProgram
import com.mikhailov.agslfx.effect.LiquidGlassProgram
import kotlin.math.roundToInt

/**
 * Состояние «подложки»: слой, в который записывается фон экрана,
 * чтобы стеклянные поверхности могли его сэмплить.
 *
 * RenderEffect в Compose работает только со **своим** содержимым слоя, поэтому размыть
 * то, что нарисовано под композаблом, штатно нельзя. Обход: фон один раз пишется
 * в [androidx.compose.ui.graphics.layer.GraphicsLayer], а стекло рисует этот слой у себя
 * внутри со сдвигом на свою позицию — и уже к нему применяет AGSL.
 */
@Stable
public class BackdropState internal constructor(
    internal val layer: androidx.compose.ui.graphics.layer.GraphicsLayer,
) {
    internal var origin: Offset by mutableStateOf(Offset.Zero)
}

/** Создаёт состояние подложки. Один на экран. */
@Composable
public fun rememberBackdrop(): BackdropState {
    val layer = rememberGraphicsLayer()
    return remember(layer) { BackdropState(layer) }
}

/**
 * Помечает композабл как источник подложки: его отрисовка попадает в слой [state]
 * и остаётся видимой как обычно.
 */
public fun Modifier.backdropSource(state: BackdropState): Modifier = this
    .onGloballyPositioned { state.origin = it.positionInRoot() }
    .drawWithContent {
        state.layer.record { this@drawWithContent.drawContent() }
        drawLayer(state.layer)
    }

/**
 * Рисует под содержимым композабла кусок подложки [state], пропущенный через AGSL-программу.
 *
 * @param program программа с `uniform shader content`.
 * @param animated запускать ли кадровые часы `uTime`. По умолчанию — только если
 *   программа их объявляет: статичному стеклу перерисовка каждый кадр не нужна.
 * @param speed множитель скорости времени.
 * @param uniforms дополнительные униформы программы.
 */
@Composable
public fun Modifier.backdropEffect(
    state: BackdropState,
    program: AgslProgram,
    animated: Boolean = AgslUniform.TIME in program.declaredUniforms,
    speed: Float = 1f,
    uniforms: AgslScope.() -> Unit = {},
): Modifier {
    val shader = Agsl.rememberShader(program)
    val usesTime = animated && AgslUniform.TIME in program.declaredUniforms
    val time = rememberShaderTime(usesTime, speed)
    val glassLayer = rememberGraphicsLayer()
    var origin by remember { mutableStateOf(Offset.Zero) }

    return this
        .onGloballyPositioned { origin = it.positionInRoot() }
        .drawWithCache {
            onDrawBehind {
                val width = if (size.width > 0f) size.width else 1f
                val height = if (size.height > 0f) size.height else 1f
                if (AgslUniform.RESOLUTION in program.declaredUniforms) {
                    shader.setFloatUniform(AgslUniform.RESOLUTION, width, height)
                }
                // Читаем время только когда оно вправду нужно: чтение State в draw-фазе
                // подписывает слой на перерисовку каждый кадр.
                val now = if (usesTime) time.value else 0f
                if (AgslUniform.TIME in program.declaredUniforms) {
                    shader.setFloatUniform(AgslUniform.TIME, now)
                }
                AgslScope(shader, size, now, this).uniforms()

                glassLayer.renderEffect = RenderEffect
                    .createRuntimeShaderEffect(shader, AgslUniform.CONTENT)
                    .asComposeRenderEffect()

                glassLayer.record(IntSize(width.roundToInt(), height.roundToInt())) {
                    translate(state.origin.x - origin.x, state.origin.y - origin.y) {
                        drawLayer(state.layer)
                    }
                }
                drawLayer(glassLayer)
            }
        }
}

/**
 * Готовое «жидкое стекло» поверх подложки: преломление у краёв, блик на фаске, подкраска.
 *
 * ```
 * val backdrop = rememberBackdrop()
 * Box(Modifier.fillMaxSize().meshGradient().backdropSource(backdrop)) {
 *     Box(Modifier.size(240.dp, 140.dp).liquidGlassBackdrop(backdrop))
 * }
 * ```
 */
@Composable
public fun Modifier.liquidGlassBackdrop(
    state: BackdropState,
    cornerRadius: Dp = 28.dp,
    thickness: Dp = 26.dp,
    refraction: Float = 22f,
    glare: Float = 0.9f,
    tint: Color = Color.White.copy(alpha = 0.07f),
): Modifier = backdropEffect(state, LiquidGlassProgram) {
    set("uCornerRadius", cornerRadius.toPx())
    set("uThickness", thickness.toPx())
    set("uRefraction", refraction)
    set("uGlare", glare)
    set("uTint", tint)
}

/** Матовое стекло поверх подложки. */
@Composable
public fun Modifier.frostedGlassBackdrop(
    state: BackdropState,
    radius: Dp = 18.dp,
    tint: Color = Color.White.copy(alpha = 0.14f),
    noise: Float = 0.02f,
): Modifier = backdropEffect(state, FrostedGlassProgram) {
    set("uRadius", radius.toPx())
    set("uTint", tint)
    set("uNoise", noise)
}
