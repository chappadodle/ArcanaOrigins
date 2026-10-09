package com.chappadodle.arcanaorigins.client;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Client-only settings (arcanaorigins-client.toml, also editable from the Mods screen). */
public final class ArcanaClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SCREEN_EFFECTS = BUILDER
            .comment("Camera shake and screen flashes from big spells.")
            .translation("arcanaorigins.configuration.screenEffects")
            .define("screenEffects", true);

    public static final ModConfigSpec.BooleanValue BLOOM = BUILDER
            .comment("Light bleeding around glowing spell effects. Needs the Veil mod; off while an Iris shader pack is on.")
            .translation("arcanaorigins.configuration.bloom")
            .define("bloom", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ArcanaClientConfig() {
    }
}
