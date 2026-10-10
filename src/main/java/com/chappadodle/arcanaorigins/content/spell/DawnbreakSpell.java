package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.server.level.ServerPlayer;


/**
 * Dawnborn's Dawnbreak (Elemental Arcana's): a small sun rises over the place the caster stands and
 * shines there for 12 seconds, burning the undead, scorching and showing up every other foe, and
 * clearing darkness from the caster and their allies (see Dawnbreaks).
 */
public class DawnbreakSpell implements Spell {
    public static final String NAME = "dawnbreak";

    @Override
    public boolean cast(ServerPlayer caster) {
        Dawnbreaks.start(caster.serverLevel(), caster, 1f);
        return true;
    }
}
