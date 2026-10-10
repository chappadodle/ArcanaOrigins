package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.server.level.ServerPlayer;

/**
 * Galeborn's Gale Dash (Elemental Arcana's, at one fixed strength): a burst of wind carries you the
 * way you look, untouchable while it lasts (see GaleDashes).
 */
public class GaleDashSpell implements Spell {
    public static final String NAME = "gale_dash";

    @Override
    public boolean cast(ServerPlayer caster) {
        GaleDashes.dash(caster);
        return true;
    }
}
