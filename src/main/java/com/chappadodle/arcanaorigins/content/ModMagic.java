package com.chappadodle.arcanaorigins.content;

import com.chappadodle.arcanaorigins.ArcanaOrigins;
import com.chappadodle.arcanaorigins.api.SpellProjectile;
import com.chappadodle.arcanaorigins.content.spell.EmberSprite;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The magic's shared registrations (Elemental Arcana's ModContent, trimmed to what the origins
 * use): the spell projectile and the spells' creatures, the statuses (Wet, Frozen), and the
 * particles.
 */
public final class ModMagic {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, ArcanaOrigins.MODID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ArcanaOrigins.MODID);
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ArcanaOrigins.MODID);
    private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, ArcanaOrigins.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<SpellProjectile>> SPELL_PROJECTILE =
            ENTITY_TYPES.register("spell_projectile", () -> EntityType.Builder.<SpellProjectile>of(SpellProjectile::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f).clientTrackingRange(4).updateInterval(10)
                    .build(ArcanaOrigins.MODID + ":spell_projectile"));
    /** Emberborn's Ember Sprite (content/spell/EmberSprite): a little fire spirit that spits bolts. */
    public static final DeferredHolder<EntityType<?>, EntityType<EmberSprite>> EMBER_SPRITE =
            ENTITY_TYPES.register("ember_sprite", () -> EntityType.Builder.<EmberSprite>of(EmberSprite::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).updateInterval(4).fireImmune()
                    .build(ArcanaOrigins.MODID + ":ember_sprite"));

    /** A glowing 4-point frost glint that twinkles out. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FROST_SPARKLE =
            PARTICLES.register("frost_sparkle", () -> new SimpleParticleType(false));
    /** A small ice fragment that tumbles and falls. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ICE_SHARD =
            PARTICLES.register("ice_shard", () -> new SimpleParticleType(false));
    /** A soft cold puff that spreads and fades. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FROST_MIST =
            PARTICLES.register("frost_mist", () -> new SimpleParticleType(false));
    /** A glowing spark that drifts up and cools as it fades. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> EMBER =
            PARTICLES.register("ember", () -> new SimpleParticleType(false));
    /** A wind curl tinted with an element's color, for Swirl rings. */
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOption>> SWIRL =
            PARTICLES.register("swirl", () -> new ParticleType<ColorParticleOption>(false) {
                @Override
                public MapCodec<ColorParticleOption> codec() {
                    return ColorParticleOption.codec(this);
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                    return ColorParticleOption.streamCodec(this);
                }
            });
    /** A soft glowing blob that shrinks, shifts colour and dissolves: the body of fire trails. */
    public static final DeferredHolder<ParticleType<?>, ParticleType<GlowParticleOptions>> FLARE =
            PARTICLES.register("flare", GlowParticleOptions::newType);
    /** A glowing streak that points along its motion and slows down. */
    public static final DeferredHolder<ParticleType<?>, ParticleType<GlowParticleOptions>> SPARK =
            PARTICLES.register("spark", GlowParticleOptions::newType);
    /** A glowing feather that flutters down. */
    public static final DeferredHolder<ParticleType<?>, ParticleType<GlowParticleOptions>> FEATHER =
            PARTICLES.register("feather", GlowParticleOptions::newType);
    /** A soft round glow that swells and fades (a blast's corona). */
    public static final DeferredHolder<ParticleType<?>, ParticleType<GlowParticleOptions>> CORONA =
            PARTICLES.register("corona", GlowParticleOptions::newType);
    /** A dark chip of rock with a glowing edge that falls and bounces. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CINDER =
            PARTICLES.register("cinder", () -> new SimpleParticleType(false));
    /** A flat glowing ring that expands along the ground and fades: an explosion's shockwave. */
    public static final DeferredHolder<ParticleType<?>, ParticleType<GlowParticleOptions>> SHOCKWAVE =
            PARTICLES.register("shockwave", GlowParticleOptions::newType);
    /** A fireball's whole explosion in one particle (see FireBlastOptions). */
    public static final DeferredHolder<ParticleType<?>, ParticleType<FireBlastOptions>> FIRE_BLAST =
            PARTICLES.register("fire_blast", FireBlastOptions::newType);
    /** Pyronado's wheels, for as long as they spin, in one particle (see PyronadoOptions). */
    public static final DeferredHolder<ParticleType<?>, ParticleType<PyronadoOptions>> PYRONADO =
            PARTICLES.register("pyronado", PyronadoOptions::newType);

    /** Frozen solid (the Freeze reaction): rooted in place, no jumping or melee damage. */
    public static final DeferredHolder<MobEffect, FrozenEffect> FROZEN = EFFECTS.register("frozen", FrozenEffect::new);
    /** Soaked by water (the Hydro aura): drips, can't stay on fire, and sets up Vaporize and Freeze. */
    public static final DeferredHolder<MobEffect, WetEffect> WET = EFFECTS.register("wet", WetEffect::new);

    private ModMagic() {
    }

    @SuppressWarnings("unused")
    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ArcanaOrigins.id(name)));
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        SOUNDS.register(modEventBus);
        EFFECTS.register(modEventBus);
        PARTICLES.register(modEventBus);
    }
}
