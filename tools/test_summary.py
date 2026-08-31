#!/usr/bin/env python3
"""
Сводка по юнит-тестам из отчётов JUnit.

Нужна не ради красоты вывода. Gradle отдаёт задачу с тестами как FROM-CACHE или
UP-TO-DATE, и в логе остаётся только «BUILD SUCCESSFUL» — по нему нельзя отличить
прошедшие тесты от ненайденных. Если тестовый сорссет случайно перестанет
подхватываться, сборка останется зелёной, а тестов не будет.

Скрипт разбирает XML-отчёты и возвращает ненулевой код, если тестов не нашлось.

    python3 tools/test_summary.py [каталог с отчётами ...]
"""
import pathlib
import sys
import xml.etree.ElementTree as ET

DEFAULT_ROOTS = ["agslfx/build/test-results", "demo/build/test-results"]

roots = sys.argv[1:] or DEFAULT_ROOTS
reports = [f for root in roots for f in pathlib.Path(root).rglob("TEST-*.xml")]

if not reports:
    sys.exit(
        "не найдено ни одного отчёта JUnit в: " + ", ".join(roots) + "\n"
        "тесты не выполнялись — проверьте, что сорссет src/test/kotlin подхватывается"
    )

total = failures = errors = skipped = 0
for report in sorted(reports):
    suite = ET.parse(report).getroot()
    total += int(suite.get("tests", 0))
    failures += int(suite.get("failures", 0))
    errors += int(suite.get("errors", 0))
    skipped += int(suite.get("skipped", 0))
    name = suite.get("name", report.stem)
    print(f"  {suite.get('tests', 0):>3}  {name}")

print(f"\nвсего тестов: {total}, провалов: {failures}, ошибок: {errors}, пропущено: {skipped}")

if total == 0:
    sys.exit("отчёты есть, но тестов в них ноль")
if failures or errors:
    sys.exit(1)
