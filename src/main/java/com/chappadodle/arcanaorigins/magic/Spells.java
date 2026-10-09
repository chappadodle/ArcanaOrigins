package com.chappadodle.arcanaorigins.magic;

import com.chappadodle.arcanaorigins.ArcanaOrigins;
import com.chappadodle.arcanaorigins.api.ProjectileSpells;
import com.chappadodle.arcanaorigins.content.spell.EmberSpriteSpell;
import com.chappadodle.arcanaorigins.content.spell.FireballSpell;
import com.chappadodle.arcanaorigins.content.spell.PyronadoSpell;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Every spell, by the name its power's JSON gives ({@code "spell": "fireball"}). */
public final class Spells {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Spell> SPELLS = new LinkedHashMap<>();

    static {
        // Emberborn (Fire).
        register(FireballSpell.NAME, ProjectileSpells.register(new FireballSpell()));
        register("ember_sprite", new EmberSpriteSpell());
        register("pyronado", new PyronadoSpell());
        // A test spell: a puff of sparks and a log line (tools/server_tests check the wiring with it).
        register("spark", caster -> {
            caster.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, caster.getX(), caster.getEyeY(), caster.getZ(),
                    12, 0.3, 0.3, 0.3, 0.05);
            LOGGER.info("[{}] spark cast by {}", ArcanaOrigins.MODID, caster.getGameProfile().getName());
            return true;
        });
    }

    private Spells() {
    }

    /** Registers every spell (this class's static block) now, before the client loads their models. */
    public static void bootstrap() {
    }

    public static void register(String name, Spell spell) {
        if (SPELLS.putIfAbsent(name, spell) != null) {
            throw new IllegalStateException("Two spells named " + name);
        }
    }

    @Nullable
    public static Spell get(String name) {
        return SPELLS.get(name);
    }

    public static Set<String> names() {
        return SPELLS.keySet();
    }

    /** Casts the spell called {@code name} (for the test command too); false if there's none or it didn't happen. */
    public static boolean cast(String name, ServerPlayer caster) {
        Spell spell = get(name);
        return spell != null && spell.cast(caster);
    }
}
