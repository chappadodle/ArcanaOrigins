package com.chappadodle.arcanaorigins.core;

import com.chappadodle.arcanaorigins.ArcanaOrigins;
import com.chappadodle.arcanaorigins.api.Bubble;
import com.chappadodle.arcanaorigins.api.SpellShield;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/** Data the spells keep on creatures (Elemental Arcana's MagicAttachments, trimmed): a magic shield, a bubble prison. */
public final class MagicAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ArcanaOrigins.MODID);

    /** A player's magic shield (Frost Shield), synced to everyone who can see them. Not saved. */
    public static final Supplier<AttachmentType<SpellShield>> SHIELD = ATTACHMENT_TYPES.register("shield",
            () -> AttachmentType.builder(SpellShield::new)
                    .sync(SpellShield.STREAM_CODEC)
                    .build());

    /** A creature held in a Bubble Prison, synced so everyone sees the bubble. */
    public static final Supplier<AttachmentType<Bubble>> BUBBLE = ATTACHMENT_TYPES.register("bubble",
            () -> AttachmentType.builder(() -> new Bubble(Vec3.ZERO, 0L, 0L))
                    .sync(Bubble.STREAM_CODEC)
                    .build());

    /** When a creature's Frozen effect ends (game time), synced so everyone sees the ice shell. */
    public static final Supplier<AttachmentType<Long>> FROZEN_UNTIL = ATTACHMENT_TYPES.register("frozen_until",
            () -> AttachmentType.builder(() -> 0L)
                    .sync(net.minecraft.network.codec.ByteBufCodecs.VAR_LONG)
                    .build());

    private MagicAttachments() {
    }
}
