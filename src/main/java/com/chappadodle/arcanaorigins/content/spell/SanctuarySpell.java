package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.server.level.ServerPlayer;

import com.chappadodle.arcanaorigins.api.SpellTargets;

/**
 * Dawnborn's Sanctuary (Elemental Arcana's): a circle of light 4 blocks across at the caster's feet,
 * for 8 seconds (see Sanctuaries). Each second it mends the caster and their allies inside, and
 * sears the undead.
 */
public class SanctuarySpell implements Spell {
    public static final String NAME = "sanctuary";

    @Override
    public boolean cast(ServerPlayer caster) {
        Sanctuaries.place(caster.serverLevel(), caster.position(), caster, 1f, target -> SpellTargets.canAffect(caster, target));
        return true;
    }
}
