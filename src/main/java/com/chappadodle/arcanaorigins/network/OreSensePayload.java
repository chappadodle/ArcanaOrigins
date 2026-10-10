package com.chappadodle.arcanaorigins.network;

import com.chappadodle.arcanaorigins.ArcanaOrigins;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/** Server to client: the ores Prismborn's Ore Sense just felt, for the client to outline (OreSenseRenderer). */
public record OreSensePayload(List<BlockPos> ores) implements CustomPacketPayload {
    public static final Type<OreSensePayload> TYPE = new Type<>(ArcanaOrigins.id("ore_sense"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OreSensePayload> STREAM_CODEC =
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list(256)).map(OreSensePayload::new, OreSensePayload::ores).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
