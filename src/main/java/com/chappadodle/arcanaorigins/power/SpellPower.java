package com.chappadodle.arcanaorigins.power;

import com.chappadodle.arcanaorigins.magic.Spells;
import com.cyberday1.neoorigins.power.builtin.base.AbstractActivePower;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code arcanaorigins:spell}: an active power (a skill key) that casts one of the spells
 * ({@code "spell": "fireball"}). NeoOrigins' AbstractActivePower gives it its key slot, its
 * cooldown ({@code cooldown_ticks}, started only when the spell happens) and its HUD icon
 * ({@code cooldown_icon}).
 */
public class SpellPower extends AbstractActivePower<SpellPower.Config> {

    public record Config(String spell, int cooldownTicks, String cooldownIcon, boolean cooldownCountdown, boolean alwaysShowIcon)
            implements AbstractActivePower.Config {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.fieldOf("spell").forGetter(Config::spell),
                Codec.INT.optionalFieldOf("cooldown_ticks", 20).forGetter(Config::cooldownTicks),
                Codec.STRING.optionalFieldOf("cooldown_icon", "").forGetter(Config::cooldownIcon),
                Codec.BOOL.optionalFieldOf("cooldown_countdown", true).forGetter(Config::cooldownCountdown),
                Codec.BOOL.optionalFieldOf("always_show_icon", true).forGetter(Config::alwaysShowIcon)
        ).apply(inst, Config::new));
    }

    @Override
    public Codec<Config> codec() {
        return Config.CODEC;
    }

    @Override
    protected boolean execute(ServerPlayer player, Config config) {
        return Spells.cast(config.spell(), player);
    }
}
