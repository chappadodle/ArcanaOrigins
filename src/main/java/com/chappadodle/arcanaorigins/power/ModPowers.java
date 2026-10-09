package com.chappadodle.arcanaorigins.power;

import com.chappadodle.arcanaorigins.ArcanaOrigins;
import com.cyberday1.neoorigins.api.power.PowerType;
import com.cyberday1.neoorigins.power.registry.PowerTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Our power types, in NeoOrigins' power type registry (so origin JSON can name them, e.g. {@code arcanaorigins:spell}). */
public final class ModPowers {
    private static final DeferredRegister<PowerType<?>> POWER_TYPES = DeferredRegister.create(PowerTypes.REGISTRY_KEY, ArcanaOrigins.MODID);

    public static final DeferredHolder<PowerType<?>, SpellPower> SPELL = POWER_TYPES.register("spell", SpellPower::new);
    public static final DeferredHolder<PowerType<?>, AuraBonusPower> AURA_BONUS = POWER_TYPES.register("aura_bonus", AuraBonusPower::new);
    public static final DeferredHolder<PowerType<?>, FrostWalkPower> FROST_WALK = POWER_TYPES.register("frost_walk", FrostWalkPower::new);

    private ModPowers() {
    }

    public static void register(IEventBus modEventBus) {
        POWER_TYPES.register(modEventBus);
    }
}
