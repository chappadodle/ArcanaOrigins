package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.server.level.ServerPlayer;

/** Emberborn's ultimate, Pyronado (Elemental Arcana's): three wheels of flame circle you for 8 seconds, burning and shoving whatever they sweep. */
public class PyronadoSpell implements Spell {
    @Override
    public boolean cast(ServerPlayer caster) {
        Pyronados.start(caster, 1f);
        return true;
    }
}
