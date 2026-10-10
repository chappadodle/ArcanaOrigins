package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.ArcanaOrigins;
import com.chappadodle.arcanaorigins.api.SpellTargets;
import com.chappadodle.arcanaorigins.content.ModMagic;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gale Dash on the server (Elemental Arcana's, at one fixed strength): a burst of wind the way the
 * caster looks (any way in mid-air), shoving away what's near where they started. While it lasts no
 * harm comes to them, its trail shoves foes it passes aside, and when it ends a tailwind speeds them
 * on. Never saved.
 */
@EventBusSubscriber(modid = ArcanaOrigins.MODID)
public final class GaleDashes {
    /** How long a dash lasts (its trail, its shove, its guard), in ticks. */
    private static final int DASH_TICKS = 8;
    private static final double SPEED = 1.5;
    /** How close to the dasher foes are shoved aside. */
    private static final double REACH = 1.8;

    /** A dash under way: when it ends and which way it goes. */
    private record Dash(long end, Vec3 direction) {
    }

    private static final Map<UUID, Dash> DASHES = new HashMap<>();

    private GaleDashes() {
    }

    public static void dash(ServerPlayer caster) {
        ServerLevel level = caster.serverLevel();
        Vec3 look = caster.getLookAngle();
        boolean airborne = !caster.onGround();
        // On the ground never into the ground: always a little lift.
        Vec3 direction = airborne ? look.normalize() : new Vec3(look.x, Math.max(look.y, 0.1), look.z).normalize();
        caster.setDeltaMovement(direction.scale(SPEED).add(0, airborne ? 0.1 : 0.35, 0));
        caster.hurtMarked = true;
        caster.resetFallDistance();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(3.0),
                target -> target != caster && SpellTargets.canAffect(caster, target))) {
            Vec3 away = target.position().subtract(caster.position()).normalize();
            target.knockback(1.0, -away.x, -away.z);
            target.hurtMarked = true;
        }
        DASHES.put(caster.getUUID(), new Dash(level.getGameTime() + DASH_TICKS, direction));
        level.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, caster.getX(), caster.getY(), caster.getZ(), 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.CLOUD, caster.getX(), caster.getY() + 0.2, caster.getZ(), 15, 0.4, 0.1, 0.4, 0.05);
        play(level, caster.position(), SoundEvents.WIND_CHARGE_BURST.value(), 1f, 1f);
        play(level, caster.position(), SoundEvents.BREEZE_SLIDE, 1f, 1.3f);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Dash dash = DASHES.get(player.getUUID());
        if (dash == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (level.getGameTime() > dash.end()) {
            // Tailwind.
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1));
            DASHES.remove(player.getUUID());
            return;
        }
        // The wind it leaves behind.
        Vec3 back = dash.direction().scale(-0.15);
        level.sendParticles(ModMagic.WIND_STREAK.get(), player.getX(), player.getY() + 0.9, player.getZ(), 0, back.x, back.y, back.z, 1);
        level.sendParticles(ModMagic.WIND_STREAK.get(), player.getX(), player.getY() + 0.4, player.getZ(), 2, 0.2, 0.3, 0.2, 0.02);
        // Foes it passes are shoved aside, off the dasher's line.
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(REACH),
                target -> target != player && target.isAlive() && SpellTargets.canAffect(player, target))) {
            double grip = Math.max(0, 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            Vec3 off = target.position().subtract(player.position()).multiply(1, 0, 1);
            Vec3 side = new Vec3(-dash.direction().z, 0, dash.direction().x);
            double sign = Math.signum(off.dot(side)) == 0 ? 1 : Math.signum(off.dot(side));
            Vec3 shove = side.scale(sign * 0.6 * grip);
            target.setDeltaMovement(target.getDeltaMovement().add(shove.x, 0.15 * grip, shove.z));
            target.hurtMarked = true;
        }
    }

    private static void play(ServerLevel level, Vec3 at, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, at.x, at.y, at.z, sound, SoundSource.PLAYERS, volume, pitch);
    }

    /** No harm comes to a dasher while the dash lasts (a command's kill still does). */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        Dash dash = DASHES.get(player.getUUID());
        if (dash != null && player.level().getGameTime() <= dash.end()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        DASHES.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        DASHES.clear();
    }
}
