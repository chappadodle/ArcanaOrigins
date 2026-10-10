package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.server.level.ServerPlayer;


/**
 * Prismborn's Prism Ward (Elemental Arcana's): crystal facets circle the caster for 6 seconds, and
 * every projectile that would hit them (arrows, fireballs, spells) is turned back at whoever shot
 * it (see PrismWards).
 */
public class PrismWardSpell implements Spell {
    public static final String NAME = "prism_ward";
    private static final int DURATION_TICKS = 120;

    @Override
    public boolean cast(ServerPlayer caster) {
        PrismWards.raise(caster, DURATION_TICKS);
        return true;
    }
}
