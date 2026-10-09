package com.chappadodle.arcanaorigins.power;

import com.chappadodle.arcanaorigins.ArcanaOrigins;
import com.chappadodle.arcanaorigins.api.ElementalReactions;
import com.cyberday1.neoorigins.api.NeoOriginsAPI;
import com.cyberday1.neoorigins.api.power.PowerConfiguration;
import com.cyberday1.neoorigins.api.power.PowerType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Locale;

/**
 * {@code arcanaorigins:aura_bonus}: a passive. Every hit its holder lands on a foe carrying an
 * element ({@code "aura": "pyro"} burning, {@code "hydro"} wet, {@code "cryo"} frosted or frozen;
 * see ElementalReactions) is {@code multiplier} times as hard (Emberborn's: burning foes, ×1.25).
 */
@EventBusSubscriber(modid = ArcanaOrigins.MODID)
public class AuraBonusPower extends PowerType<AuraBonusPower.Config> {

    public record Config(String aura, float multiplier) implements PowerConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.fieldOf("aura").forGetter(Config::aura),
                Codec.FLOAT.optionalFieldOf("multiplier", 1.25f).forGetter(Config::multiplier)
        ).apply(inst, Config::new));
    }

    @Override
    public Codec<Config> codec() {
        return Config.CODEC;
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) {
            return;
        }
        LivingEntity target = event.getEntity();
        ElementalReactions.Aura aura = ElementalReactions.auraOf(target);
        if (aura == null) {
            return;
        }
        float[] multiplier = {1f};
        NeoOriginsAPI.forEachOfType(attacker, AuraBonusPower.class, config -> {
            if (config.aura().toUpperCase(Locale.ROOT).equals(aura.name())) {
                multiplier[0] *= config.multiplier();
            }
        });
        if (multiplier[0] != 1f) {
            event.setAmount(event.getAmount() * multiplier[0]);
        }
    }
}
