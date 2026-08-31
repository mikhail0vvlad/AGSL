# AGSL FX

Библиотека шейдерных эффектов для Jetpack Compose на AGSL. 8 шейдеров, ни одного PNG —
всё считает GPU.

Делал, потому что надоели «стеклянные» блоки, которые на самом деле ничего не преломляют,
и свечения в виде картинок на девять плотностей.

![Каталог эффектов](docs/preview/_sheet.png)

Изначально эффектов было шестнадцать — половину выкинул: зерно, глитч, кинескоп, растр,
пиксели, звёзды. Вместе они не складывались ни во что цельное. Осталась геометрия, стекло
и жёлто-красная палитра. Проект личный, к Яндексу отношения не имеет.

Нужен Android 13+ — `RuntimeShader` появился именно там.

## Быстрый старт

```kotlin
dependencies { implementation(project(":agslfx")) }
```

Эффект — обычный `Modifier`:

```kotlin
Box(Modifier.shimmer())
Box(Modifier.fillMaxSize().meshGradient())
Box(Modifier.liquidFill(progress = 0.6f))
```

Свой шейдер подключается тем же ядром:

```kotlin
val MyProgram = AgslProgram(
    name = "My effect",
    body = """
        uniform shader content;
        uniform float uTime;

        half4 main(float2 fragCoord) {
            float wave = sin(fragCoord.y * 0.05 + uTime * 3.0);
            return content.eval(fragCoord + float2(wave * 20.0, 0.0));
        }
    """
)

Box(Modifier.agslEffect(MyProgram))
```

`uResolution` и `uTime` проставляются сами. В теле доступны `agslNoise`, `agslFbm`,
`agslLuma`, `agslRotate` и ещё пара функций из `Agsl.PRELUDE`.

## Каталог

| | API | Что делает |
|---|---|---|
| <img src="docs/preview/shimmer.png" width="110"> | `Modifier.shimmer()` | Диагональный блик, основа skeleton-загрузки |
| <img src="docs/preview/frosted-glass.png" width="110"> | `Modifier.frostedGlass()` | Матовое стекло |
| <img src="docs/preview/liquid-glass.png" width="110"> | `Modifier.liquidGlass()` | Стеклянная линза: преломление у краёв + блик |
| <img src="docs/preview/duotone.png" width="110"> | `Modifier.duotone()` | Яркость раскладывается между двумя цветами |
| <img src="docs/preview/water-ripple.png" width="110"> | `Modifier.waterRipple()` `Modifier.touchRipple()` | Волна по прогрессу или по касанию |
| <img src="docs/preview/mesh-gradient.png" width="110"> | `Modifier.meshGradient()` | Живой фон из четырёх плавающих точек |
| <img src="docs/preview/animated-border.png" width="110"> | `Modifier.animatedBorder()` | Вращающаяся обводка со свечением |
| <img src="docs/preview/liquid-fill.png" width="110"> | `Modifier.liquidFill(progress)` | Прогресс с волной на поверхности |

Плюс `ShimmerPlaceholder()` и `Modifier.liquidGlassBackdrop(state)` — стекло, которое
преломляет **фон под собой**, а не свой контент. Штатными средствами Compose так нельзя,
поэтому фон пишется в `GraphicsLayer` и панель рисует его у себя со сдвигом. В демо
её можно таскать пальцем.

Цвета по умолчанию фирменные, но каждый перекрывается параметром:

```kotlin
Box(Modifier.duotone())                                 // чёрный → жёлтый
Box(Modifier.duotone(shadow = Navy, highlight = Cyan))  // свои
```

## Проверка шейдеров без устройства

AGSL — диалект SkSL, а Skia доступна из Python. Значит те же исходники компилируются
и рисуются на CI, без эмулятора:

```bash
pip install skia-python
python3 tools/validate_agsl.py     # ненулевой код при ошибке
python3 tools/render_previews.py   # рендерит docs/preview/*.png
```

Все превью выше отрисованы вторым скриптом из тех же констант, что компилируются
на устройстве, — картинки не могут разойтись с кодом.

## Сборка

```bash
./gradlew :demo:assembleDebug      # витрина: галерея, слайдеры, исходник каждого шейдера
./gradlew :agslfx:publishToMavenLocal
```

Ключ подписи в репозиторий не кладу. Для подписанного `release` опишите свой keystore
в `local.properties` (`signing.storeFile`, `signing.storePassword`, `signing.keyAlias`,
`signing.keyPassword`) — без них release просто соберётся неподписанным.

## Лицензия

MIT — см. [LICENSE](LICENSE).
