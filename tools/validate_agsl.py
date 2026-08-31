#!/usr/bin/env python3
"""
Оффлайн-валидатор AGSL-шейдеров библиотеки.

Вытаскивает тела всех AgslProgram из исходников Kotlin, подклеивает Agsl.PRELUDE
и компилирует каждую программу настоящим компилятором SkSL (AGSL — это диалект SkSL,
на котором работает Skia внутри Android).

    pip install skia-python
    python3 tools/validate_agsl.py

Возвращает ненулевой код выхода, если хотя бы один шейдер не компилируется.
"""
import pathlib
import re
import sys

try:
    import skia
except ImportError as error:
    sys.exit(f"""не удалось импортировать skia-python: {error}
  pip install skia-python
  на Linux дополнительно нужны системные библиотеки OpenGL:
  sudo apt-get install -y libgl1 libglu1-mesa libegl1""")

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "agslfx" / "src" / "main" / "kotlin"

prelude_file = SRC / "com" / "mikhailov" / "agslfx" / "core" / "Agsl.kt"
prelude_match = re.search(r'PRELUDE:\s*String\s*=\s*"""(.*?)"""', prelude_file.read_text(encoding="utf-8"), re.S)
if not prelude_match:
    sys.exit("не нашёл Agsl.PRELUDE")
PRELUDE = prelude_match.group(1)

PROGRAM_RE = re.compile(r'name\s*=\s*"([^"]+)"\s*,\s*body\s*=\s*"""(.*?)"""', re.S)
UNIFORM_RE = re.compile(r"^\s*uniform\s+\w+\s+(\w+)\s*;", re.M)


def dead_uniforms(body):
    """Униформы, объявленные, но ни разу не использованные.

    Некоторые сборки Skia выбрасывают такие объявления при компиляции, и тогда
    setFloatUniform для них падает с IllegalArgumentException уже на устройстве.
    """
    dead = []
    for name in UNIFORM_RE.findall(body):
        without_declaration = UNIFORM_RE.sub("", body)
        if not re.search(r"\b" + re.escape(name) + r"\b", without_declaration):
            dead.append(name)
    return dead

failures = 0
checked = 0
for path in sorted(SRC.rglob("*.kt")):
    text = path.read_text(encoding="utf-8")
    for name, body in PROGRAM_RE.findall(text):
        checked += 1
        source = PRELUDE + body
        try:
            skia.RuntimeEffect.MakeForShader(source)
        except Exception as error:  # noqa: BLE001
            failures += 1
            rel = path.relative_to(ROOT)
            print(f"[FAIL] {name}  ({rel})")
            print("       " + str(error).replace("\n", "\n       "))
        else:
            dead = dead_uniforms(body)
            if dead:
                failures += 1
                print(f"[FAIL] {name}: униформы объявлены, но не используются: {', '.join(dead)}")
            else:
                print(f"[ ok ] {name}")

print(f"\n{checked - failures}/{checked} шейдеров скомпилировалось")
sys.exit(1 if failures else 0)
