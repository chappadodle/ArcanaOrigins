package com.chappadodle.arcanaorigins.power;

import com.chappadodle.arcanaorigins.content.spell.Freezing;
import com.cyberday1.neoorigins.api.power.PowerConfiguration;
import com.cyberday1.neoorigins.api.power.PowerType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code arcanaorigins:frost_walk}: a passive. While its holder stands on solid ground (or ice), still
 * water within {@code radius} blocks round their feet freezes over into frosted ice, as Frost Walker
 * boots do, and melts back behind them. Not while sneaking, so they can still fetch water.
 */
public class FrostWalkPower extends PowerType<FrostWalkPower.Config> {

    public record Config(int radius) implements PowerConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.optionalFieldOf("radius", 2).forGetter(Config::radius)
        ).apply(inst, Config::new));
    }

    @Override
    public Codec<Config> codec() {
        return Config.CODEC;
    }

    @Override
    public void onTick(ServerPlayer player, Config config) {
        if (player.tickCount % 4 != 0 || !player.onGround() || player.isShiftKeyDown() || player.isInWater() || player.isSpectator()) {
            return;
        }
        Freezing.freezeWater(player.serverLevel(), player.blockPosition().below(), config.radius());
    }
}
