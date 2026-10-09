package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Tideborn's ultimate, Tsunami (Elemental Arcana's): a wall of water rises in front of the caster
 * and rolls away the way they face, 16 blocks in 1.6 seconds, sweeping up what it meets and putting
 * out fires (see Tsunamis). It needs a little open ground to rise on: without it nothing happens,
 * and no cooldown is taken.
 */
public class TsunamiSpell implements Spell {
    @Override
    public boolean cast(ServerPlayer caster) {
        if (!Tsunamis.start(caster, 1f)) {
            caster.displayClientMessage(Component.translatable("message.arcanaorigins.tsunami.no_room"), true);
            return false;
        }
        caster.clearFire();
        return true;
    }
}
