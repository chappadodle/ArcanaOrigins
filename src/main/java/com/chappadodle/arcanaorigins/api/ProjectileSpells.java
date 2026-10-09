package com.chappadodle.arcanaorigins.api;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** The spells that fly as projectiles, by name (a projectile syncs its spell's name). */
public final class ProjectileSpells {
    private static final Map<String, ProjectileSpell> SPELLS = new LinkedHashMap<>();

    private ProjectileSpells() {
    }

    public static <S extends ProjectileSpell> S register(S spell) {
        if (SPELLS.putIfAbsent(spell.name(), spell) != null) {
            throw new IllegalStateException("Two projectile spells named " + spell.name());
        }
        return spell;
    }

    @Nullable
    public static ProjectileSpell get(String name) {
        return SPELLS.get(name);
    }

    public static Collection<ProjectileSpell> all() {
        return SPELLS.values();
    }
}
