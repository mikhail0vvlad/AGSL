package com.mikhailov.agslfx.demo.catalog

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikhailov.agslfx.background.AuroraProgram
import com.mikhailov.agslfx.background.MeshGradientProgram
import com.mikhailov.agslfx.background.StarfieldProgram
import com.mikhailov.agslfx.background.auroraBackground
import com.mikhailov.agslfx.background.meshGradient
import com.mikhailov.agslfx.background.starfield
import com.mikhailov.agslfx.component.DissolveVisibility
import com.mikhailov.agslfx.component.ShimmerPlaceholder
import com.mikhailov.agslfx.component.backdropSource
import com.mikhailov.agslfx.component.liquidGlassBackdrop
import com.mikhailov.agslfx.component.rememberBackdrop
import com.mikhailov.agslfx.core.AgslProgram
import com.mikhailov.agslfx.decor.AnimatedBorderProgram
import com.mikhailov.agslfx.decor.LiquidFillProgram
import com.mikhailov.agslfx.decor.animatedBorder
import com.mikhailov.agslfx.decor.liquidFill
import com.mikhailov.agslfx.effect.CrtProgram
import com.mikhailov.agslfx.effect.DissolveProgram
import com.mikhailov.agslfx.effect.DuotoneProgram
import com.mikhailov.agslfx.effect.FilmGrainProgram
import com.mikhailov.agslfx.effect.FrostedGlassProgram
import com.mikhailov.agslfx.effect.GlitchProgram
import com.mikhailov.agslfx.effect.HalftoneProgram
import com.mikhailov.agslfx.effect.LiquidGlassProgram
import com.mikhailov.agslfx.effect.PixelateProgram
import com.mikhailov.agslfx.effect.ShimmerProgram
import com.mikhailov.agslfx.effect.WaterRippleProgram
import com.mikhailov.agslfx.effect.crt
import com.mikhailov.agslfx.effect.dissolve
import com.mikhailov.agslfx.effect.duotone
import com.mikhailov.agslfx.effect.filmGrain
import com.mikhailov.agslfx.effect.frostedGlass
import com.mikhailov.agslfx.effect.glitch
import com.mikhailov.agslfx.effect.halftone
import com.mikhailov.agslfx.effect.liquidGlass
import com.mikhailov.agslfx.effect.pixelate
import com.mikhailov.agslfx.effect.shimmer
import com.mikhailov.agslfx.effect.touchRipple
import com.mikhailov.agslfx.demo.ui.SampleArtwork
import kotlin.math.roundToInt

/** Настраиваемый параметр демо — рисуется слайдером на экране эффекта. */
class DemoParam(
    val key: String,
    val label: String,
    val min: Float,
    val max: Float,
    val default: Float,
)

typealias ParamValues = Map<String, Float>

fun ParamValues.f(key: String): Float = this[key] ?: 0f

/** Одна карточка каталога. */
class DemoEntry(
    val id: String,
    val title: String,
    val description: String,
    val group: String,
    val program: AgslProgram,
    val params: List<DemoParam> = emptyList(),
    val hint: String? = null,
    val preview: @Composable (ParamValues, Modifier) -> Unit,
)

private const val GROUP_CONTENT = "Эффекты над контентом"
private const val GROUP_BACKGROUND = "Живые фоны"
private const val GROUP_COMPONENTS = "Компоненты и декор"

val DemoCatalog: List<DemoEntry> = listOf(

    DemoEntry(
        id = "shimmer",
        title = "Shimmer",
        description = "Диагональный блик по контенту. Основа skeleton-загрузки: " +
            "полоса считается в UV-пространстве, поэтому не зависит от размера элемента.",
        group = GROUP_CONTENT,
        program = ShimmerProgram,
        params = listOf(
            DemoParam("width", "Ширина полосы", 0.03f, 0.6f, 0.18f),
            DemoParam("speed", "Скорость", 0.1f, 2.5f, 0.6f),
            DemoParam("angle", "Наклон", -1.5f, 1.5f, 0.6f),
        ),
        preview = { p, m ->
            SampleArtwork(
                m.shimmer(
                    width = p.f("width"),
                    speed = p.f("speed"),
                    angleRadians = p.f("angle"),
                )
            )
        },
    ),

    DemoEntry(
        id = "grain",
        title = "Film grain",
        description = "Плёночное зерно. Шум привязан к сетке пикселей и обновляется " +
            "с частотой 24 кадра — как настоящая киноплёнка, а не как дрожащий телевизор.",
        group = GROUP_CONTENT,
        program = FilmGrainProgram,
        params = listOf(
            DemoParam("intensity", "Сила", 0f, 0.6f, 0.16f),
            DemoParam("size", "Размер зерна", 1f, 6f, 1.5f),
            DemoParam("bias", "Только полутона", 0f, 1f, 0.6f),
        ),
        preview = { p, m ->
            SampleArtwork(
                m.filmGrain(
                    intensity = p.f("intensity"),
                    size = p.f("size"),
                    midtoneBias = p.f("bias"),
                )
            )
        },
    ),

    DemoEntry(
        id = "glitch",
        title = "Glitch",
        description = "Построчный сдвиг блоков плюс расхождение RGB-каналов. " +
            "Рисунок сбоя детерминирован по хешу от номера строки и кадра.",
        group = GROUP_CONTENT,
        program = GlitchProgram,
        params = listOf(
            DemoParam("intensity", "Сила", 0f, 1f, 0.5f),
            DemoParam("block", "Высота блока", 4f, 80f, 24f),
            DemoParam("split", "Расхождение RGB", 0f, 24f, 6f),
        ),
        preview = { p, m ->
            SampleArtwork(
                m.glitch(
                    intensity = p.f("intensity"),
                    blockSizePx = p.f("block"),
                    channelSplitPx = p.f("split"),
                )
            )
        },
    ),

    DemoEntry(
        id = "dissolve",
        title = "Dissolve",
        description = "Растворение по fbm-шуму с раскалённой кромкой. " +
            "Прогресс можно анимировать — получается «сгорающий» переход между состояниями.",
        group = GROUP_CONTENT,
        program = DissolveProgram,
        params = listOf(
            DemoParam("progress", "Прогресс", 0f, 1f, 0.45f),
            DemoParam("scale", "Масштаб шума", 1f, 20f, 6f),
            DemoParam("edge", "Ширина кромки", 0.01f, 0.3f, 0.09f),
        ),
        preview = { p, m ->
            SampleArtwork(
                m.dissolve(
                    progress = p.f("progress"),
                    scale = p.f("scale"),
                    edgeWidth = p.f("edge"),
                )
            )
        },
    ),

    DemoEntry(
        id = "ripple",
        title = "Water ripple",
        description = "Расходящаяся волна: сэмплы смещаются вдоль радиуса, на гребне " +
            "добавляется блик. Амплитуда гаснет экспоненциально от фронта волны.",
        group = GROUP_CONTENT,
        program = WaterRippleProgram,
        hint = "Нажмите на картинку",
        params = listOf(
            DemoParam("amplitude", "Амплитуда", 4f, 90f, 34f),
            DemoParam("frequency", "Частота", 2f, 40f, 12f),
        ),
        preview = { p, m ->
            SampleArtwork(
                m.touchRipple(
                    amplitudePx = p.f("amplitude"),
                    frequency = p.f("frequency"),
                )
            )
        },
    ),

    DemoEntry(
        id = "frosted",
        title = "Frosted glass",
        description = "Матовое стекло: 16 отсчётов по золотой спирали со случайным " +
            "поворотом ядра. Дешевле полноценного гауссиана и не даёт «звёздочек».",
        group = GROUP_CONTENT,
        program = FrostedGlassProgram,
        params = listOf(
            DemoParam("radius", "Радиус", 0f, 40f, 14f),
            DemoParam("noise", "Матовость", 0f, 0.2f, 0.03f),
        ),
        preview = { p, m ->
            SampleArtwork(
                m.frostedGlass(
                    radius = p.f("radius").dp,
                    noise = p.f("noise"),
                )
            )
        },
    ),

    DemoEntry(
        id = "liquid-glass",
        title = "Liquid glass",
        description = "Стеклянная линза: нормаль берётся как градиент SDF скруглённого " +
            "прямоугольника, у краёв контент преломляется, на фаске загорается блик.",
        group = GROUP_CONTENT,
        program = LiquidGlassProgram,
        params = listOf(
            DemoParam("radius", "Скругление", 0f, 120f, 40f),
            DemoParam("thickness", "Толщина фаски", 4f, 120f, 46f),
            DemoParam("refraction", "Преломление", 0f, 60f, 24f),
            DemoParam("glare", "Блик", 0f, 2f, 0.9f),
        ),
        preview = { p, m ->
            SampleArtwork(
                m.liquidGlass(
                    cornerRadius = p.f("radius").dp,
                    thickness = p.f("thickness").dp,
                    refraction = p.f("refraction"),
                    glare = p.f("glare"),
                )
            )
        },
    ),

    DemoEntry(
        id = "duotone",
        title = "Duotone",
        description = "Яркость раскладывается между двумя цветами. Работает в «прямом» " +
            "цветовом пространстве: контент сначала разпремультиплицируется.",
        group = GROUP_CONTENT,
        program = DuotoneProgram,
        params = listOf(
            DemoParam("amount", "Сила", 0f, 1f, 1f),
            DemoParam("contrast", "Контраст", 0.4f, 2.5f, 1.15f),
        ),
        preview = { p, m ->
            SampleArtwork(
                m.duotone(
                    amount = p.f("amount"),
                    contrast = p.f("contrast"),
                )
            )
        },
    ),

    DemoEntry(
        id = "halftone",
        title = "Halftone",
        description = "Полиграфический растр: радиус точки на повёрнутой сетке " +
            "зависит от яркости пикселя.",
        group = GROUP_CONTENT,
        program = HalftoneProgram,
        params = listOf(
            DemoParam("cell", "Шаг сетки", 3f, 30f, 10f),
            DemoParam("angle", "Поворот", 0f, 1.57f, 0.4f),
            DemoParam("amount", "Сила", 0f, 1f, 1f),
        ),
        preview = { p, m ->
            SampleArtwork(
                m.halftone(
                    cellSizePx = p.f("cell"),
                    angleRadians = p.f("angle"),
                    amount = p.f("amount"),
                )
            )
        },
    ),

    DemoEntry(
        id = "pixelate",
        title = "Pixelate",
        description = "Квантование координат сэмплирования плюс зазор между «пикселями» — " +
            "получается LED-панель.",
        group = GROUP_CONTENT,
        program = PixelateProgram,
        params = listOf(
            DemoParam("size", "Размер пикселя", 2f, 60f, 16f),
            DemoParam("gap", "Зазор", 0f, 0.6f, 0.12f),
        ),
        preview = { p, m ->
            SampleArtwork(
                m.pixelate(
                    sizePx = p.f("size"),
                    gap = p.f("gap"),
                )
            )
        },
    ),

    DemoEntry(
        id = "crt",
        title = "CRT",
        description = "Кинескоп: бочкообразная дисторсия, расхождение каналов к краям, " +
            "строчная развёртка, виньетка и лёгкое мерцание.",
        group = GROUP_CONTENT,
        program = CrtProgram,
        params = listOf(
            DemoParam("curvature", "Кривизна", 0f, 1.2f, 0.4f),
            DemoParam("scanline", "Строки", 0f, 1f, 0.35f),
            DemoParam("density", "Плотность строк", 0.3f, 4f, 1.6f),
            DemoParam("aberration", "Аберрация", 0f, 6f, 1.6f),
        ),
        preview = { p, m ->
            SampleArtwork(
                m.crt(
                    curvature = p.f("curvature"),
                    scanline = p.f("scanline"),
                    density = p.f("density"),
                    aberrationPx = p.f("aberration"),
                )
            )
        },
    ),

    DemoEntry(
        id = "aurora",
        title = "Aurora",
        description = "Северное сияние из двух лент fbm-шума. Дизеринг по хешу убирает " +
            "полосы на плавных градиентах.",
        group = GROUP_BACKGROUND,
        program = AuroraProgram,
        params = listOf(
            DemoParam("intensity", "Насыщенность", 0f, 1.5f, 0.9f),
            DemoParam("scale", "Масштаб", 0.5f, 6f, 2.2f),
            DemoParam("speed", "Скорость", 0.1f, 3f, 1f),
        ),
        preview = { p, m ->
            Box(
                m.auroraBackground(
                    intensity = p.f("intensity"),
                    scale = p.f("scale"),
                    speed = p.f("speed"),
                )
            )
        },
    ),

    DemoEntry(
        id = "mesh",
        title = "Mesh gradient",
        description = "Четыре цветные точки плавают по площади, цвет пикселя — среднее " +
            "с весами по обратному расстоянию.",
        group = GROUP_BACKGROUND,
        program = MeshGradientProgram,
        params = listOf(
            DemoParam("falloff", "Резкость", 0.8f, 5f, 2.2f),
            DemoParam("spread", "Разлёт точек", 0.05f, 0.6f, 0.32f),
            DemoParam("speed", "Скорость", 0.05f, 2f, 0.35f),
        ),
        preview = { p, m ->
            Box(
                m.meshGradient(
                    falloff = p.f("falloff"),
                    spread = p.f("spread"),
                    speed = p.f("speed"),
                )
            )
        },
    ),

    DemoEntry(
        id = "starfield",
        title = "Starfield",
        description = "Три параллаксных слоя звёзд на хеш-сетке, у самых ярких " +
            "включается крестообразный блик.",
        group = GROUP_BACKGROUND,
        program = StarfieldProgram,
        params = listOf(
            DemoParam("density", "Плотность", 4f, 40f, 14f),
            DemoParam("drift", "Дрейф", 0f, 2f, 0.35f),
            DemoParam("twinkle", "Мерцание", 0f, 1f, 0.9f),
        ),
        preview = { p, m ->
            Box(
                m.starfield(
                    density = p.f("density"),
                    drift = p.f("drift"),
                    twinkle = p.f("twinkle"),
                )
            )
        },
    ),

    DemoEntry(
        id = "border",
        title = "Animated border",
        description = "Коническая градиентная обводка по контуру SDF-прямоугольника " +
            "с мягким свечением наружу.",
        group = GROUP_COMPONENTS,
        program = AnimatedBorderProgram,
        params = listOf(
            DemoParam("width", "Толщина", 1f, 12f, 2.5f),
            DemoParam("radius", "Скругление", 0f, 60f, 24f),
            DemoParam("glow", "Свечение", 0f, 1.5f, 0.7f),
        ),
        preview = { p, m ->
            Box(m, contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .fillMaxWidth(0.8f)
                        .height(120.dp)
                        .clip(RoundedCornerShape(p.f("radius").dp))
                        .background(Color(0xFF141828))
                        .animatedBorder(
                            width = p.f("width").dp,
                            cornerRadius = p.f("radius").dp,
                            glow = p.f("glow"),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("PRO", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
    ),

    DemoEntry(
        id = "liquid-fill",
        title = "Liquid fill",
        description = "Прогресс с волной на поверхности. Две синусоиды разной частоты " +
            "дают несинхронное «плескание».",
        group = GROUP_COMPONENTS,
        program = LiquidFillProgram,
        params = listOf(
            DemoParam("progress", "Прогресс", 0f, 1f, 0.6f),
            DemoParam("amplitude", "Волна", 0f, 0.12f, 0.03f),
            DemoParam("waves", "Число волн", 0.5f, 5f, 1.6f),
        ),
        preview = { p, m ->
            Box(m, contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .fillMaxWidth(0.85f)
                        .height(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .liquidFill(
                            progress = p.f("progress"),
                            amplitude = p.f("amplitude"),
                            waves = p.f("waves"),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${(p.f("progress") * 100).roundToInt()}%",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
    ),

    DemoEntry(
        id = "skeleton",
        title = "Shimmer placeholder",
        description = "Готовый компонент-скелетон: контейнер с бликом, который " +
            "перерисовывается без рекомпозиции.",
        group = GROUP_COMPONENTS,
        program = ShimmerProgram,
        params = listOf(
            DemoParam("speed", "Скорость", 0.1f, 2f, 0.7f),
        ),
        preview = { p, m ->
            Column(
                modifier = m.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ShimmerPlaceholder(
                    Modifier.size(72.dp),
                    shape = RoundedCornerShape(20.dp),
                    speed = p.f("speed"),
                )
                ShimmerPlaceholder(
                    Modifier
                        .fillMaxWidth()
                        .height(18.dp),
                    shape = RoundedCornerShape(9.dp),
                    speed = p.f("speed"),
                )
                ShimmerPlaceholder(
                    Modifier
                        .fillMaxWidth(0.7f)
                        .height(18.dp),
                    shape = RoundedCornerShape(9.dp),
                    speed = p.f("speed"),
                )
                ShimmerPlaceholder(
                    Modifier
                        .fillMaxWidth(0.45f)
                        .height(18.dp),
                    shape = RoundedCornerShape(9.dp),
                    speed = p.f("speed"),
                )
            }
        },
    ),

    DemoEntry(
        id = "dissolve-visibility",
        title = "DissolveVisibility",
        description = "Замена AnimatedVisibility: появление и исчезновение через " +
            "шумовую маску вместо альфы.",
        group = GROUP_COMPONENTS,
        program = DissolveProgram,
        hint = "Нажмите, чтобы переключить",
        params = emptyList(),
        preview = { _, m ->
            var visible by remember { mutableStateOf(true) }
            Box(
                modifier = m.clickable { visible = !visible },
                contentAlignment = Alignment.Center,
            ) {
                DissolveVisibility(
                    visible = visible,
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(150.dp)
                        .clip(RoundedCornerShape(24.dp)),
                ) {
                    SampleArtwork(Modifier.fillMaxSize())
                }
                if (!visible) {
                    Text(
                        "нажмите ещё раз",
                        color = Color(0xFF6C7590),
                        fontSize = 13.sp,
                    )
                }
            }
        },
    ),

    DemoEntry(
        id = "backdrop",
        title = "Backdrop glass",
        description = "Стекло, которое действительно преломляет ФОН, а не свой контент. " +
            "Фон пишется в GraphicsLayer, панель рисует этот слой у себя со сдвигом " +
            "и применяет к нему AGSL.",
        group = GROUP_COMPONENTS,
        program = LiquidGlassProgram,
        hint = "Перетаскивайте панель",
        params = listOf(
            DemoParam("refraction", "Преломление", 0f, 60f, 26f),
            DemoParam("thickness", "Толщина фаски", 4f, 80f, 30f),
            DemoParam("glare", "Блик", 0f, 2f, 0.9f),
        ),
        preview = { p, m ->
            val backdrop = rememberBackdrop()
            var dragX by remember { mutableFloatStateOf(0f) }
            var dragY by remember { mutableFloatStateOf(0f) }

            val transition = rememberInfiniteTransition(label = "backdrop")
            val phase by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
                label = "phase",
            )

            Box(
                modifier = m
                    .auroraBackground(
                        colorA = Color(0xFF1F6FB2),
                        colorB = Color(0xFF7A3FD1),
                        colorC = Color(0xFFFF6FA5),
                        speed = 0.6f + phase * 0f,
                    )
                    .backdropSource(backdrop),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .offset { IntOffset(dragX.roundToInt(), dragY.roundToInt()) }
                        .fillMaxWidth(0.6f)
                        .height(120.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .liquidGlassBackdrop(
                            state = backdrop,
                            cornerRadius = 28.dp,
                            thickness = p.f("thickness").dp,
                            refraction = p.f("refraction"),
                            glare = p.f("glare"),
                        )
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                dragX += dragAmount.x
                                dragY += dragAmount.y
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "GLASS",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
    ),
)

val DemoGroups: List<String> = listOf(GROUP_CONTENT, GROUP_BACKGROUND, GROUP_COMPONENTS)

/** Значения параметров по умолчанию. */
fun DemoEntry.defaultValues(): Map<String, Float> = params.associate { it.key to it.default }

