package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.server.level.ServerPlayer;


/**
 * Stormborn's Stormcall (Elemental Arcana's): a
 * storm cloud gathers over the caster and follows them for 10 seconds, soaking what stands under it
 * and striking the foes around them (see Stormcalls). The caster fights on while it rages.
 */
public class StormcallSpell implements Spell {
    public static final String NAME = "stormcall";

    @Override
    public boolean cast(ServerPlayer caster) {
        Stormcalls.start(caster, 1f);
        return true;
    }
}
