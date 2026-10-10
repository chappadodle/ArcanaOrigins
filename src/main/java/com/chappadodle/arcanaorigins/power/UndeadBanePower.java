package com.chappadodle.arcanaorigins.power;

import com.cyberday1.neoorigins.api.power.PowerConfiguration;
import com.cyberday1.neoorigins.api.power.PowerType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;

/**
 * {@code arcanaorigins:undead_bane}: Dawnborn's Daylight Soul. The undead within {@code radius}
 * blocks of the player catch fire, every {@code interval} ticks, for {@code burn_ticks}.
 */
public class UndeadBanePower extends PowerType<UndeadBanePower.Config> {

    public record Config(double radius, int interval, int burnTicks) implements PowerConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.DOUBLE.optionalFieldOf("radius", 4.0).forGetter(Config::radius),
                Codec.INT.optionalFieldOf("interval", 20).forGetter(Config::interval),
                Codec.INT.optionalFieldOf("burn_ticks", 60).forGetter(Config::burnTicks)
        ).apply(inst, Config::new));
    }

    @Override
    public Codec<Config> codec() {
        return Config.CODEC;
    }

    @Override
    public void onTick(ServerPlayer player, Config config) {
        if (player.isSpectator() || player.tickCount % Math.max(1, config.interval()) != 0) {
            return;
        }
        ServerLevel level = player.serverLevel();
        double radius = config.radius();
        for (LivingEntity undead : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius),
                e -> e.isAlive() && e.getType().is(EntityTypeTags.UNDEAD) && e.distanceTo(player) <= radius && !e.fireImmune())) {
            undead.igniteForTicks(config.burnTicks());
            level.sendParticles(ParticleTypes.END_ROD, undead.getX(), undead.getY(0.6), undead.getZ(), 2, 0.2, 0.3, 0.2, 0.01);
        }
    }
}
