#!/usr/bin/env python3
"""Copies source files from Elemental Arcana into Arcana Origins, renaming its packages and names.

    python3 tools/port_from_ea.py <path under EA's java root> [...]

Paths are relative to com/chappadodle/elementalarcana (e.g. content/WetEffect.java); each lands at
the same place under com/chappadodle/arcanaorigins. Rewritten: the package root, the mod class
(ElementalArcana -> ArcanaOrigins), the central registries (ModContent -> ModMagic, which holds the
entity, particle, effect and sound registrations this mod keeps) and the "elementalarcana" mod id in
resource locations. Everything else (dropped systems: mana, levels, MagicData...) is left for the
compiler to point out, and trimmed by hand.

Elemental Arcana is the same author's, under MIT (see the README).
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
EA = ROOT.parent / "elemental-arcana"
EA_JAVA = EA / "src/main/java/com/chappadodle/elementalarcana"
AO_JAVA = ROOT / "src/main/java/com/chappadodle/arcanaorigins"

RENAMES = [
    ("com.chappadodle.elementalarcana", "com.chappadodle.arcanaorigins"),
    ("ElementalArcana.MODID", "ArcanaOrigins.MODID"),
    ("ElementalArcana.id(", "ArcanaOrigins.id("),
    ("import com.chappadodle.arcanaorigins.ElementalArcana;", "import com.chappadodle.arcanaorigins.ArcanaOrigins;"),
    ("com.chappadodle.arcanaorigins.content.ModContent", "com.chappadodle.arcanaorigins.content.ModMagic"),
    ("ModContent.", "ModMagic."),
    ('"elementalarcana:', '"arcanaorigins:'),
]


def port(relative):
    source = EA_JAVA / relative
    text = source.read_text()
    for old, new in RENAMES:
        text = text.replace(old, new)
    text = re.sub(r"\bElementalArcana\b", "ArcanaOrigins", text)
    target = AO_JAVA / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    if target.exists():
        print(f"skipped (already there): {relative}")
        return
    target.write_text(text)
    print(f"ported {relative}")


if __name__ == "__main__":
    for path in sys.argv[1:]:
        port(path)
