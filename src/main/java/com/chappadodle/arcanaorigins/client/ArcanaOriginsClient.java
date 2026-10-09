package com.chappadodle.arcanaorigins.client;

import com.chappadodle.arcanaorigins.client.particle.HydroDropParticle;
import com.chappadodle.arcanaorigins.client.particle.HydroStreamEmitter;
import com.chappadodle.arcanaorigins.client.particle.TsunamiParticle;
import com.chappadodle.arcanaorigins.client.particle.WaterBurstParticle;
import com.chappadodle.arcanaorigins.client.visual.WaterBeams;
import com.chappadodle.arcanaorigins.api.ShieldSpell;
import com.chappadodle.arcanaorigins.magic.Spells;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import com.chappadodle.arcanaorigins.ArcanaOrigins;
import com.chappadodle.arcanaorigins.api.ProjectileSpell;
import com.chappadodle.arcanaorigins.api.ProjectileSpells;
import com.chappadodle.arcanaorigins.client.decal.Decals;
import com.chappadodle.arcanaorigins.client.particle.BubbleCastEmitter;
import com.chappadodle.arcanaorigins.client.particle.CinderParticle;
import com.chappadodle.arcanaorigins.client.particle.IceShatterEmitter;
import com.chappadodle.arcanaorigins.client.particle.EmberParticle;
import com.chappadodle.arcanaorigins.client.particle.FireBlastEmitter;
import com.chappadodle.arcanaorigins.client.particle.FrostMistParticle;
import com.chappadodle.arcanaorigins.client.particle.FrostSparkleParticle;
import com.chappadodle.arcanaorigins.client.particle.GlowParticle;
import com.chappadodle.arcanaorigins.client.particle.IceShardParticle;
import com.chappadodle.arcanaorigins.client.particle.PyronadoEmitter;
import com.chappadodle.arcanaorigins.client.particle.WindStreakParticle;
import com.chappadodle.arcanaorigins.content.ModMagic;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

import java.util.HashSet;
import java.util.Set;

/** The client side: the spells' renderers, particles and models, and the drawing they need each frame. */
public final class ArcanaOriginsClient {
    private ArcanaOriginsClient() {
    }

    /** Registration events (the mod bus). */
    @EventBusSubscriber(modid = ArcanaOrigins.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class Setup {
        private Setup() {
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModMagic.SPELL_PROJECTILE.get(), SpellProjectileRenderer::new);
            event.registerEntityRenderer(ModMagic.EMBER_SPRITE.get(), EmberSpriteRenderer::new);
        }

        @SubscribeEvent
        public static void registerParticles(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModMagic.FROST_SPARKLE.get(), FrostSparkleParticle.Provider::new);
            event.registerSpriteSet(ModMagic.ICE_SHARD.get(), IceShardParticle.Provider::new);
            event.registerSpriteSet(ModMagic.FROST_MIST.get(), FrostMistParticle.Provider::new);
            event.registerSpriteSet(ModMagic.EMBER.get(), EmberParticle.Provider::new);
            event.registerSpriteSet(ModMagic.SWIRL.get(), WindStreakParticle.SwirlProvider::new);
            event.registerSpriteSet(ModMagic.FLARE.get(), sprites -> new GlowParticle.Provider(GlowParticle.Kind.FLARE, sprites));
            event.registerSpriteSet(ModMagic.SPARK.get(), sprites -> new GlowParticle.Provider(GlowParticle.Kind.SPARK, sprites));
            event.registerSpriteSet(ModMagic.FEATHER.get(), sprites -> new GlowParticle.Provider(GlowParticle.Kind.FEATHER, sprites));
            event.registerSpriteSet(ModMagic.CINDER.get(), CinderParticle.Provider::new);
            event.registerSpriteSet(ModMagic.SHOCKWAVE.get(), sprites -> new GlowParticle.Provider(GlowParticle.Kind.RING, sprites));
            event.registerSpriteSet(ModMagic.CORONA.get(), sprites -> new GlowParticle.Provider(GlowParticle.Kind.CORONA, sprites));
            event.registerSpecial(ModMagic.FIRE_BLAST.get(), new FireBlastEmitter.Provider());
            event.registerSpecial(ModMagic.PYRONADO.get(), new PyronadoEmitter.Provider());
            event.registerSpecial(ModMagic.ICE_SHATTER.get(), new IceShatterEmitter.Provider());
            event.registerSpriteSet(ModMagic.HYDRO_DROP.get(), HydroDropParticle.Provider::new);
            event.registerSpecial(ModMagic.HYDRO_STREAM.get(), new HydroStreamEmitter.Provider());
            event.registerSpecial(ModMagic.WATER_BURST.get(), new WaterBurstParticle.Provider());
            event.registerSpecial(ModMagic.TSUNAMI.get(), new TsunamiParticle.Provider());
            event.registerSpecial(ModMagic.BUBBLE_CAST.get(), new BubbleCastEmitter.Provider());
        }

        /** Every projectile spell's 3D models. */
        @SubscribeEvent
        public static void registerModels(ModelEvent.RegisterAdditional event) {
            Set<ResourceLocation> models = new HashSet<>();
            for (ProjectileSpell spell : ProjectileSpells.all()) {
                models.addAll(spell.models());
            }
            // Shield spells' orbiting shards.
            for (String name : Spells.names()) {
                if (Spells.get(name) instanceof ShieldSpell shield && shield.shardModel() != null) {
                    models.add(shield.shardModel());
                }
            }
            models.forEach(model -> event.register(ModelResourceLocation.standalone(model)));
        }

        @SubscribeEvent
        public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(WispModel.LAYER, WispModel::createLayer);
        }

        @SubscribeEvent
        public static void registerGuiLayers(RegisterGuiLayersEvent event) {
            event.registerAboveAll(ArcanaOrigins.id("screen_flash"), ScreenEffects.FLASH_LAYER);
            event.registerAbove(VanillaGuiLayers.PLAYER_HEALTH, ArcanaOrigins.id("shield_hearts"), new ShieldHeartsLayer());
        }
    }

    /** Every frame (the game bus). */
    @EventBusSubscriber(modid = ArcanaOrigins.MODID, value = Dist.CLIENT)
    public static final class Frame {
        private Frame() {
        }

        @SubscribeEvent
        public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
            if (event.getEntity() instanceof Player player) {
                ShieldRenderer.renderShards(player, event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource());
            }
            BubbleRenderer.render(event.getEntity(), event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
            FrozenShells.render(event.getEntity(), event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource());
        }

        @SubscribeEvent
        public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
            ScreenEffects.onCameraAngles(event);
        }

        // Additive particles change the blend function and vanilla doesn't set it back; put it back
        // before anything drawn later assumes the default.
        @SubscribeEvent
        public static void afterParticles(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
                Decals.render(event);
            }
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
                WaterBeams.render(event);
            }
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableCull();
                Bloom.renderParticles(event.getCamera(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
            }
        }
    }
}
