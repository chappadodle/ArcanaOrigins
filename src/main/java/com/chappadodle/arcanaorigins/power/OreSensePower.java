package com.chappadodle.arcanaorigins.power;

import com.chappadodle.arcanaorigins.network.OreSensePayload;
import com.cyberday1.neoorigins.api.power.PowerConfiguration;
import com.cyberday1.neoorigins.api.power.PowerType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code arcanaorigins:ore_sense}: Prismborn's Ore Sense. Every {@code interval} ticks the ores
 * within {@code radius} blocks (the {@code c:ores} tag) glow through the rock for the player alone,
 * for a couple of seconds (client/OreSenseRenderer), with a faint chime.
 */
public class OreSensePower extends PowerType<OreSensePower.Config> {
    private static final int MAX_ORES = 256;

    public record Config(int radius, int interval) implements PowerConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.optionalFieldOf("radius", 8).forGetter(Config::radius),
                Codec.INT.optionalFieldOf("interval", 100).forGetter(Config::interval)
        ).apply(inst, Config::new));
    }

    @Override
    public Codec<Config> codec() {
        return Config.CODEC;
    }

    @Override
    public void onTick(ServerPlayer player, Config config) {
        if (player.isSpectator() || player.tickCount % Math.max(20, config.interval()) != 0) {
            return;
        }
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        int r = config.radius();
        List<BlockPos> ores = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            if (pos.distSqr(center) <= r * r && level.getBlockState(pos).is(Tags.Blocks.ORES)) {
                ores.add(pos.immutable());
                if (ores.size() >= MAX_ORES) {
                    break;
                }
            }
        }
        if (ores.isEmpty()) {
            return;
        }
        PacketDistributor.sendToPlayer(player, new OreSensePayload(ores));
        player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.4f, 1.8f);
    }
}
