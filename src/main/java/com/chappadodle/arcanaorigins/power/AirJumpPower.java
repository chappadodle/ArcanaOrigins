package com.chappadodle.arcanaorigins.power;

import com.chappadodle.arcanaorigins.content.ModMagic;
import com.cyberday1.neoorigins.power.builtin.base.AbstractActivePower;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * {@code arcanaorigins:air_jump}: Galeborn's Updraft. Bound to the jump key in the power's JSON
 * ({@code "key": "key.jump"}), so pressing jump in mid-air leaps again ({@code velocity} up),
 * keeping the way the player was going.
 *
 * NeoOrigins' {@code add_velocity} pushes the server's copy of the player's motion, which for a
 * player is stale (the client moves them), so the boost was lost; this sets the motion outright,
 * from how far they moved in the last tick.
 */
public class AirJumpPower extends AbstractActivePower<AirJumpPower.Config> {
    private static final double MAX_CARRY = 0.6;

    public record Config(double velocity, int cooldownTicks, String cooldownIcon, boolean cooldownCountdown, boolean alwaysShowIcon)
            implements AbstractActivePower.Config {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.DOUBLE.optionalFieldOf("velocity", 0.7).forGetter(Config::velocity),
                Codec.INT.optionalFieldOf("cooldown_ticks", 30).forGetter(Config::cooldownTicks),
                Codec.STRING.optionalFieldOf("cooldown_icon", "").forGetter(Config::cooldownIcon),
                Codec.BOOL.optionalFieldOf("cooldown_countdown", true).forGetter(Config::cooldownCountdown),
                Codec.BOOL.optionalFieldOf("always_show_icon", false).forGetter(Config::alwaysShowIcon)
        ).apply(inst, Config::new));
    }

    @Override
    public Codec<Config> codec() {
        return Config.CODEC;
    }

    @Override
    protected boolean execute(ServerPlayer player, Config config) {
        if (player.onGround() || player.isInWater() || player.isFallFlying() || player.isPassenger() || player.getAbilities().flying) {
            return false;
        }
        Vec3 carry = new Vec3(player.getX() - player.xo, 0, player.getZ() - player.zo);
        if (carry.length() > MAX_CARRY) {
            carry = carry.normalize().scale(MAX_CARRY);
        }
        player.setDeltaMovement(carry.x, config.velocity(), carry.z);
        player.hurtMarked = true;
        player.resetFallDistance();
        ServerLevel level = player.serverLevel();
        level.sendParticles(ModMagic.WIND_STREAK.get(), player.getX(), player.getY(), player.getZ(), 8, 0.3, 0.05, 0.3, 0.05);
        level.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, player.getX(), player.getY(), player.getZ(), 1, 0, 0, 0, 0);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BREEZE_JUMP, SoundSource.PLAYERS, 0.8f, 1.3f);
        return true;
    }
}
