package com.chappadodle.arcanaorigins.magic;

import net.minecraft.server.level.ServerPlayer;

/** A spell an origin casts from a skill key: fixed in strength (no levels, no mana), on its power's cooldown. */
public interface Spell {
    /** Casts it for {@code caster}; false if nothing happened (then no cooldown is taken). */
    boolean cast(ServerPlayer caster);
}
