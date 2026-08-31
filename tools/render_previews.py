#!/usr/bin/env python3
"""
Оффлайн-рендер превью всех AGSL-эффектов библиотеки.

AGSL — это диалект SkSL, а Skia доступна из Python, поэтому те же самые исходники
шейдеров можно отрисовать без устройства и эмулятора. Значения униформ подставляются
в исходник как const: так не приходится повторять skia-шную раскладку uniform-буфера.

    pip install skia-python
    python3 tools/render_previews.py

Результат: docs/preview/<id>.png и docs/preview/_sheet.png
"""
import math
import pathlib
import re
import sys

try:
    import skia
except ImportError:
    sys.exit("нужен skia-python:  pip install skia-python")

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "agslfx" / "src" / "main" / "kotlin"
OUT = ROOT / "docs" / "preview"
SIZE = 360

PRELUDE = re.search(
    r'PRELUDE:\s*String\s*=\s*"""(.*?)"""',
    (SRC / "com" / "mikhailov" / "agslfx" / "core" / "Agsl.kt").read_text(encoding="utf-8"),
    re.S,
).group(1)

PROGRAM_RE = re.compile(r'name\s*=\s*"([^"]+)"\s*,\s*body\s*=\s*"""(.*?)"""', re.S)
UNIFORM_RE = re.compile(r"^\s*uniform\s+(float|float2|float3|float4|half4|int)\s+(\w+)\s*;", re.M)


def literal(kind, value):
    if kind == "int":
        return str(int(value))
    if isinstance(value, (int, float)):
        value = [value]
    parts = ", ".join(f"{float(v):.6f}" for v in value)
    return f"{kind}({parts})" if kind != "float" else f"{float(value[0]):.6f}"


def inline_uniforms(body, values):
    """Заменяет объявления uniform на const с конкретными значениями."""
    missing = []

    def repl(match):
        kind, name = match.group(1), match.group(2)
        if name not in values:
            missing.append(name)
            return match.group(0)
        return f"const {kind} {name} = {literal(kind, values[name])};"

    result = UNIFORM_RE.sub(repl, body)
    if missing:
        raise KeyError("нет значений для униформ: " + ", ".join(missing))
    return result


def sample_artwork(size):
    """Та же тестовая картинка, что и в демо-приложении."""
    surface = skia.Surface(size, size)
    with surface as canvas:
        paint = skia.Paint(Shader=skia.GradientShader.MakeLinear(
            points=[(0.0, 0.0), (float(size), float(size))],
            colors=[0xFF000000, 0xFF1A1A1A, 0xFFFC3F1D, 0xFFFFCC00],
        ))
        canvas.drawPaint(paint)
        canvas.drawCircle(size * 0.74, size * 0.26, size * 0.17, skia.Paint(Color=0xE6FFCC00))
        canvas.drawCircle(size * 0.18, size * 0.86, size * 0.42, skia.Paint(Color=0x8C000000))
        canvas.drawCircle(size * 0.55, size * 0.62, size * 0.30, skia.Paint(Color=0x24FFFFFF))
        try:
            font = skia.Font(skia.Typeface.MakeFromName("DejaVu Sans", skia.FontStyle.Bold()), size * 0.16)
            text = skia.TextBlob("AGSL", font)
            canvas.drawTextBlob(text, size * 0.28, size * 0.55, skia.Paint(Color=0xFFFFFFFF))
        except Exception:  # шрифта может не быть — превью останется без подписи
            pass
    return surface.makeImageSnapshot()


R = float(SIZE)
CENTER = [R / 2, R / 2]
COMMON = {"uResolution": [R, R], "uTime": 1.6}

# Значения униформ для превью — повторяют дефолты из публичного API библиотеки.
PRESETS = {
    "Shimmer":         dict(uHighlight=[1, 1, 1, 0.55], uWidth=0.18, uAngle=0.6, uSpeed=0.6, uTime=0.45),
    "Water ripple":    dict(uCenter=CENTER, uProgress=0.35, uAmplitude=34.0,
                            uFrequency=12.0, uDecay=0.14),
    "Frosted glass":   dict(uRadius=14.0, uTint=[1, 1, 1, 0.12], uNoise=0.03),
    "Liquid glass":    dict(uCornerRadius=90.0, uThickness=70.0, uRefraction=26.0,
                            uGlare=1.0, uTint=[1, 1, 1, 0.06]),
    "Duotone":         dict(uShadow=[0, 0, 0, 1], uHighlight=[1.0, 0.8, 0.0, 1],
                            uAmount=1.0, uContrast=1.15),
    "Mesh gradient":   dict(uColor0=[1.0, 0.8, 0.0, 1], uColor1=[0.988, 0.247, 0.114, 1],
                            uColor2=[1, 1, 1, 1], uColor3=[0.102, 0.102, 0.102, 1],
                            uFalloff=2.2, uSpread=0.32, uTime=3.0),
    "Animated border": dict(uColor0=[1.0, 0.8, 0.0, 1], uColor1=[0.988, 0.247, 0.114, 1],
                            uColor2=[1, 1, 1, 1], uWidth=6.0, uCornerRadius=48.0,
                            uGlow=0.8, uTime=1.0),
    "Liquid fill":     dict(uFill=[1.0, 0.8, 0.0, 1], uTrack=[0.078, 0.078, 0.078, 1],
                            uFoam=[1, 1, 1, 0.65], uProgress=0.6, uAmplitude=0.05,
                            uWaves=1.6, uVertical=0.0, uTime=2.0),
}

ORDER = [
    "Mesh gradient", "Liquid fill", "Animated border", "Shimmer",
    "Water ripple", "Frosted glass", "Liquid glass", "Duotone",
]


def main():
    programs = {}
    for path in sorted(SRC.rglob("*.kt")):
        for name, body in PROGRAM_RE.findall(path.read_text(encoding="utf-8")):
            programs[name] = body

    OUT.mkdir(parents=True, exist_ok=True)
    artwork = sample_artwork(SIZE)
    art_shader = artwork.makeShader(skia.TileMode.kClamp, skia.TileMode.kClamp,
                                    skia.SamplingOptions(skia.FilterMode.kLinear))

    rendered = []
    for name in ORDER:
        body = programs.get(name)
        if body is None:
            print(f"[skip] {name}: не найден в исходниках")
            continue

        values = dict(COMMON)
        values.update(PRESETS[name])
        source = PRELUDE + inline_uniforms(body, values)

        effect = skia.RuntimeEffect.MakeForShader(source)
        needs_content = "uniform shader content" in body
        if needs_content:
            shader = effect.makeShader(skia.Data.MakeEmpty(), art_shader, 1)
        else:
            shader = effect.makeShader(skia.Data.MakeEmpty())

        surface = skia.Surface(SIZE, SIZE)
        with surface as canvas:
            canvas.clear(0xFF000000)
            canvas.drawPaint(skia.Paint(Shader=shader))
        image = surface.makeImageSnapshot()

        slug = re.sub(r"[^a-z0-9]+", "-", name.lower()).strip("-")
        image.save(str(OUT / f"{slug}.png"), skia.kPNG)
        rendered.append((name, image))
        print(f"[ ok ] {name} -> docs/preview/{slug}.png")

    # Контактный лист для README
    cols = 4
    rows = math.ceil(len(rendered) / cols)
    pad, label = 10, 26
    sheet_w = cols * SIZE + (cols + 1) * pad
    sheet_h = rows * (SIZE + label) + (rows + 1) * pad
    sheet = skia.Surface(sheet_w, sheet_h)
    with sheet as canvas:
        canvas.clear(0xFF000000)
        font = None
        try:
            font = skia.Font(skia.Typeface.MakeFromName("DejaVu Sans", skia.FontStyle.Normal()), 18)
        except Exception:
            pass
        for index, (name, image) in enumerate(rendered):
            col, row = index % cols, index // cols
            x = pad + col * (SIZE + pad)
            y = pad + row * (SIZE + label + pad)
            canvas.drawImage(image, x, y)
            if font is not None:
                canvas.drawString(name, x + 2, y + SIZE + 19, font, skia.Paint(Color=0xFF8C8C8C))
    sheet.makeImageSnapshot().save(str(OUT / "_sheet.png"), skia.kPNG)
    print(f"\nконтактный лист: docs/preview/_sheet.png ({len(rendered)} эффектов)")


if __name__ == "__main__":
    main()
