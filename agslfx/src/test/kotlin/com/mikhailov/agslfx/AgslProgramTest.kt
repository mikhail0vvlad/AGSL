package com.mikhailov.agslfx

import com.mikhailov.agslfx.background.MeshGradientProgram
import com.mikhailov.agslfx.core.Agsl
import com.mikhailov.agslfx.core.AgslProgram
import com.mikhailov.agslfx.core.AgslUniform
import com.mikhailov.agslfx.decor.AnimatedBorderProgram
import com.mikhailov.agslfx.decor.LiquidFillProgram
import com.mikhailov.agslfx.effect.DuotoneProgram
import com.mikhailov.agslfx.effect.FrostedGlassProgram
import com.mikhailov.agslfx.effect.LiquidGlassProgram
import com.mikhailov.agslfx.effect.ShimmerProgram
import com.mikhailov.agslfx.effect.WaterRippleProgram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Тесты на разбор и согласованность AGSL-программ.
 *
 * Компиляцию самих шейдеров эти тесты не проверяют — для неё есть
 * `tools/validate_agsl.py`, который гоняет настоящий компилятор SkSL на CI.
 * Здесь проверяется то, что ломается тихо: разбор униформ, на который
 * опирается ядро, и согласованность каталога.
 */
class AgslProgramTest {

    /** Программы, сэмплящие исходный контент через `uniform shader content`. */
    private val contentPrograms = listOf(
        ShimmerProgram,
        WaterRippleProgram,
        FrostedGlassProgram,
        LiquidGlassProgram,
        DuotoneProgram,
    )

    /** Генеративные программы: рисуют сами, контент не читают. */
    private val generativePrograms = listOf(
        MeshGradientProgram,
        AnimatedBorderProgram,
        LiquidFillProgram,
    )

    private val allPrograms = contentPrograms + generativePrograms

    @Test
    fun `declaredUniforms собирает все объявленные имена`() {
        val program = AgslProgram(
            name = "test",
            body = """
                uniform shader content;
                uniform float2 uResolution;
                uniform float uTime;
                uniform half4 uTint;

                half4 main(float2 fragCoord) { return content.eval(fragCoord); }
            """,
        )

        assertEquals(
            setOf("content", "uResolution", "uTime", "uTint"),
            program.declaredUniforms,
        )
    }

    @Test
    fun `declaredUniforms не путает объявление с использованием`() {
        val program = AgslProgram(
            name = "test",
            body = """
                uniform float uAmount;

                half4 main(float2 fragCoord) {
                    float uNotAUniform = uAmount * 2.0;
                    return half4(uNotAUniform);
                }
            """,
        )

        assertEquals(setOf("uAmount"), program.declaredUniforms)
    }

    @Test
    fun `source подклеивает prelude только когда его просят`() {
        val body = "half4 main(float2 c) { return half4(1.0); }"

        assertTrue(AgslProgram("c", body).source.startsWith(Agsl.PRELUDE))
        assertEquals(body, AgslProgram("c", body, usesPrelude = false).source)
    }

    @Test
    fun `имена программ уникальны`() {
        val names = allPrograms.map { it.name }
        assertEquals(
            "имена программ дублируются: ${names.groupBy { it }.filterValues { it.size > 1 }.keys}",
            names.size,
            names.toSet().size,
        )
    }

    /**
     * Мёртвая униформа — объявленная, но ни разу не использованная. Некоторые сборки
     * Skia выбрасывают такие при компиляции, и тогда `setFloatUniform` для неё падает
     * уже на устройстве, в рантайме. Ловим на сборке.
     */
    @Test
    fun `в программах нет мёртвых униформ`() {
        allPrograms.forEach { program ->
            val withoutDeclarations = UNIFORM_DECLARATION.replace(program.body, "")
            program.declaredUniforms.forEach { uniform ->
                assertTrue(
                    "${program.name}: униформа $uniform объявлена, но не используется",
                    Regex("\\b${Regex.escape(uniform)}\\b").containsMatchIn(withoutDeclarations),
                )
            }
        }
    }

    @Test
    fun `эффекты над контентом объявляют uniform shader content`() {
        contentPrograms.forEach { program ->
            assertTrue(
                "${program.name} должна сэмплить контент",
                AgslUniform.CONTENT in program.declaredUniforms,
            )
        }
    }

    @Test
    fun `генеративные программы контент не читают`() {
        generativePrograms.forEach { program ->
            assertFalse(
                "${program.name} не должна зависеть от контента",
                AgslUniform.CONTENT in program.declaredUniforms,
            )
        }
    }

    /**
     * Ядро проставляет uResolution и uTime само, сверяясь с [AgslProgram.declaredUniforms].
     * Если имя в шейдере разойдётся с константой — время просто перестанет идти, молча.
     */
    @Test
    fun `анимированные программы объявляют uTime под ожидаемым именем`() {
        val animated = allPrograms.filter { "uTime" in it.body }
        assertTrue("ни одна программа не использует время — проверьте тест", animated.isNotEmpty())
        animated.forEach { program ->
            assertTrue(
                "${program.name}: uTime используется, но не объявлен",
                AgslUniform.TIME in program.declaredUniforms,
            )
        }
    }

    @Test
    fun `униформы кроме content следуют соглашению об имени`() {
        allPrograms.forEach { program ->
            program.declaredUniforms
                .filter { it != AgslUniform.CONTENT }
                .forEach { uniform ->
                    assertTrue(
                        "${program.name}: униформа $uniform должна начинаться с 'u'",
                        uniform.startsWith("u"),
                    )
                }
        }
    }

    @Test
    fun `prelude содержит функции, обещанные в документации`() {
        listOf(
            "agslHash11", "agslHash12", "agslHash22", "agslNoise", "agslFbm",
            "agslRotate", "agslLuma", "agslSdRoundRect", "agslPremul", "agslUnpremul",
        ).forEach { function ->
            assertTrue("PRELUDE не содержит $function", function in Agsl.PRELUDE)
        }
    }

    /**
     * AGSL — профиль, близкий к GLSL ES 2.0: границы циклов обязаны быть известны
     * на этапе компиляции. Цикл по переменной компилируется в исключение на первом кадре.
     */
    @Test
    fun `циклы в prelude имеют константные границы`() {
        LOOP_HEADER.findAll(Agsl.PRELUDE).forEach { match ->
            val bound = match.groupValues[1]
            assertTrue(
                "граница цикла '$bound' не константа — AGSL такой цикл не скомпилирует",
                bound.toIntOrNull() != null,
            )
        }
    }

    private companion object {
        val UNIFORM_DECLARATION = Regex("""^\s*uniform\s+\w+\s+\w+\s*;""", RegexOption.MULTILINE)
        val LOOP_HEADER = Regex("""for\s*\([^;]*;[^<>]*[<>]=?\s*([\w.]+)\s*;""")
    }
}
