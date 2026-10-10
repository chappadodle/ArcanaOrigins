package com.chappadodle.arcanaorigins.content;

import com.chappadodle.arcanaorigins.ArcanaOrigins;
import com.chappadodle.arcanaorigins.api.SpellProjectile;
import com.chappadodle.arcanaorigins.content.spell.EmberSprite;
import com.chappadodle.arcanaorigins.content.spell.StormeyeEntity;
import com.chappadodle.arcanaorigins.content.spell.GeodeSentinel;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
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
    /** Galeborn's Stormeye tornado (see StormeyeEntity): never saved. */
    public static final DeferredHolder<EntityType<?>, EntityType<StormeyeEntity>> STORMEYE =
            ENTITY_TYPES.register("stormeye", () -> EntityType.Builder.<StormeyeEntity>of(StormeyeEntity::new, MobCategory.MISC)
                    .sized(1f, 2f).clientTrackingRange(10).updateInterval(2).fireImmune().noSave()
                    .build(ArcanaOrigins.MODID + ":stormeye"));
    /** Prismborn's Geode Sentinel (see GeodeSentinel): never saved. */
    public static final DeferredHolder<EntityType<?>, EntityType<GeodeSentinel>> GEODE_SENTINEL =
            ENTITY_TYPES.register("geode_sentinel", () -> EntityType.Builder.<GeodeSentinel>of(GeodeSentinel::new, MobCategory.MISC)
                    .sized(1.2f, 2.6f).clientTrackingRange(10).updateInterval(5).fireImmune().noSave()
                    .build(ArcanaOrigins.MODID + ":geode_sentinel"));

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
    /** A pale curl of wind that glides along its velocity and fades. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WIND_STREAK =
            PARTICLES.register("wind_streak", () -> new SimpleParticleType(false));
    /** A Wind Blade's whole impact in one particle (see WindCutOptions). */
    public static final DeferredHolder<ParticleType<?>, ParticleType<WindCutOptions>> WIND_CUT =
            PARTICLES.register("wind_cut", WindCutOptions::newType);
    /** A Tremor's front, for as long as it runs, in one particle (see TremorOptions). */
    public static final DeferredHolder<ParticleType<?>, ParticleType<TremorOptions>> TREMOR =
            PARTICLES.register("tremor", TremorOptions::newType);
    /** A bolt of lightning, drawn on each client (see ArcOptions). */
    public static final DeferredHolder<ParticleType<?>, ParticleType<ArcOptions>> ARC =
            PARTICLES.register("arc", ArcOptions::newType);
    /** Stormcall's cloud, for as long as it lasts, in one particle (see StormcloudOptions). */
    public static final DeferredHolder<ParticleType<?>, ParticleType<StormcloudOptions>> STORMCLOUD =
            PARTICLES.register("stormcloud", StormcloudOptions::newType);
    /** Smite's pillar of light, in one particle (see SmiteOptions). */
    public static final DeferredHolder<ParticleType<?>, ParticleType<SmiteOptions>> SMITE =
            PARTICLES.register("smite", SmiteOptions::newType);
    /** Dawnbreak's sun, for as long as it shines, in one particle (see DawnbreakOptions). */
    public static final DeferredHolder<ParticleType<?>, ParticleType<DawnbreakOptions>> DAWN_SUN =
            PARTICLES.register("dawn_sun", DawnbreakOptions::newType);
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
    /** A water droplet that flies along its velocity, sags a little and fades. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HYDRO_DROP =
            PARTICLES.register("hydro_drop", () -> new SimpleParticleType(false));
    /** Hydro Jet's stream, one tick of it in one packet (see HydroStreamOptions). */
    public static final DeferredHolder<ParticleType<?>, ParticleType<HydroStreamOptions>> HYDRO_STREAM =
            PARTICLES.register("hydro_stream", HydroStreamOptions::newType);
    /** A burst of water cubes (see WaterBurstOptions). */
    public static final DeferredHolder<ParticleType<?>, ParticleType<WaterBurstOptions>> WATER_BURST =
            PARTICLES.register("water_burst", WaterBurstOptions::newType);
    /** Tsunami's wave, for as long as it rolls, in one particle (see TsunamiOptions). */
    public static final DeferredHolder<ParticleType<?>, ParticleType<TsunamiOptions>> TSUNAMI =
            PARTICLES.register("tsunami", TsunamiOptions::newType);
    /** An icicle's whole shatter in one particle (see IceShatterOptions). */
    public static final DeferredHolder<ParticleType<?>, ParticleType<IceShatterOptions>> ICE_SHATTER =
            PARTICLES.register("ice_shatter", IceShatterOptions::newType);
    /** Bubble Prison's cast: a stream of bubbles from the hand to the target (BubbleCastEmitter). */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BUBBLE_CAST =
            PARTICLES.register("bubble_cast", () -> new SimpleParticleType(false));
    /** Pyronado's wheels, for as long as they spin, in one particle (see PyronadoOptions). */
    public static final DeferredHolder<ParticleType<?>, ParticleType<PyronadoOptions>> PYRONADO =
            PARTICLES.register("pyronado", PyronadoOptions::newType);

    /** An icicle shattering: Elemental Arcana's one synthesized sound the player kept (tools/gen_sounds). */
    public static final DeferredHolder<SoundEvent, SoundEvent> ICICLE_IMPACT = sound("spell.icicle.impact");

    /** Frozen solid (the Freeze reaction): rooted in place, no jumping or melee damage. */
    public static final DeferredHolder<MobEffect, FrozenEffect> FROZEN = EFFECTS.register("frozen", FrozenEffect::new);
    /** Soaked by water (the Hydro aura): drips, can't stay on fire, and sets up Vaporize and Freeze. */
    public static final DeferredHolder<MobEffect, WetEffect> WET = EFFECTS.register("wet", WetEffect::new);

    private ModMagic() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ArcanaOrigins.id(name)));
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        SOUNDS.register(modEventBus);
        EFFECTS.register(modEventBus);
        PARTICLES.register(modEventBus);
        modEventBus.addListener(ModMagic::registerAttributes);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(GEODE_SENTINEL.get(), GeodeSentinel.createAttributes().build());
    }
}
