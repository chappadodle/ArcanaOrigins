package com.chappadodle.arcanaorigins;

import com.chappadodle.arcanaorigins.content.ModMagic;
import com.chappadodle.arcanaorigins.core.MagicAttachments;
import com.chappadodle.arcanaorigins.magic.Spells;
import com.chappadodle.arcanaorigins.power.ModPowers;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * Arcana Origins (docs/superpowers/specs/2026-10-09-arcana-origins-design.md): Elemental Arcana's
 * magic as origins for NeoOrigins. Each element is an origin; its spells are NeoOrigins active
 * powers of our own type, {@code arcanaorigins:spell}, on the player's skill keys.
 */
@Mod(ArcanaOrigins.MODID)
public class ArcanaOrigins {
    public static final String MODID = "arcanaorigins";

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public ArcanaOrigins(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, com.chappadodle.arcanaorigins.client.ArcanaClientConfig.SPEC);
        Spells.bootstrap();
        ModMagic.register(modEventBus);
        MagicAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModPowers.register(modEventBus);
    }
}
