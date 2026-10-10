package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.ArcanaOrigins;
import com.chappadodle.arcanaorigins.api.Element;
import com.chappadodle.arcanaorigins.api.ProjectileSpell;
import com.chappadodle.arcanaorigins.api.SpellDamage;
import com.chappadodle.arcanaorigins.api.SpellProjectile;
import com.chappadodle.arcanaorigins.api.SpellTargets;
import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Stoneborn's Boulder (Elemental Arcana's, at one fixed strength): a heavy boulder hurled in an arc
 * that bounces once and crushes whatever it lands on, shoving everything nearby away and staggering
 * the creature it hit.
 */
public class BoulderSpell implements Spell, ProjectileSpell {
    public static final String NAME = "boulder";

    private static final ResourceLocation MODEL = ArcanaOrigins.id("spell/boulder");
    private static final float SPEED = 1.4f;
    private static final float GRAVITY = 0.05f;
    // A little upward lift so a throw at the crosshair arcs over, not into, the ground.
    private static final double LIFT = 0.1;
    private static final double RADIUS = 1.8;
    private static final float DAMAGE = 8f;
    private static final double KNOCKBACK = 1.0;
    private static final int STAGGER_TICKS = 15;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public boolean cast(ServerPlayer caster) {
        SpellProjectile boulder = SpellProjectile.throwFor(caster, this, SPEED, 1f);
        boulder.setGravity(GRAVITY);
        boulder.setBounces(1);
        boulder.setDeltaMovement(boulder.getDeltaMovement().add(0, LIFT, 0));
        boulder.hasImpulse = true;
        return true;
    }

    @Override
    public ParticleOptions trailParticle() {
        return rubble();
    }

    private static ParticleOptions rubble() {
        return new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState());
    }

    @Override
    public ResourceLocation model() {
        return MODEL;
    }

    @Override
    public List<ResourceLocation> models() {
        return List.of(MODEL);
    }

    @Override
    public int lifetimeTicks() {
        return 100;
    }

    @Override
    public void flightParticles(SpellProjectile boulder) {
        if (boulder.tickCount % 2 == 0) {
            boulder.spawnParticleAround(rubble(), 0.2 * boulder.visualScale(), Vec3.ZERO);
        }
    }

    // ---- impact (server) ----

    @Override
    public void onHitEntity(SpellProjectile boulder, EntityHitResult hit) {
        crush(boulder, hit.getEntity(), boulder.impactPoint(hit));
    }

    @Override
    public void onHitBlock(SpellProjectile boulder, BlockHitResult hit) {
        crush(boulder, null, hit.getLocation());
    }

    /** The crush: damages and shoves everything in the blast, and staggers the creature it hit. */
    private static void crush(SpellProjectile boulder, @Nullable Entity directHit, Vec3 at) {
        ServerLevel level = (ServerLevel) boulder.level();
        Entity owner = boulder.getOwner();
        float damage = DAMAGE * boulder.power();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(RADIUS),
                e -> e != owner && e.isAlive() && (e == directHit || SpellTargets.canAffect(owner, e)
                        && e.getBoundingBox().getCenter().distanceTo(at) <= RADIUS + e.getBbWidth() / 2))) {
            double falloff = target == directHit ? 1.0
                    : 1.0 - 0.5 * Math.min(1.0, target.getBoundingBox().getCenter().distanceTo(at) / RADIUS);
            SpellDamage.hurtMultiHit(target, SpellDamage.source(level, Element.EARTH, boulder, owner), (float) (damage * falloff));
            Vec3 away = target.position().subtract(at).normalize();
            target.knockback(KNOCKBACK, -away.x, -away.z);
            target.hurtMarked = true;
            if (target == directHit) {
                stun(target, STAGGER_TICKS);
            }
        }
        level.sendParticles(rubble(), at.x, at.y + 0.2, at.z, 30 + (int) (RADIUS * 8), RADIUS * 0.4, 0.3, RADIUS * 0.4, 0.15);
        level.sendParticles(ParticleTypes.POOF, at.x, at.y + 0.2, at.z, 6, RADIUS * 0.3, 0.2, RADIUS * 0.3, 0.02);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1.4f, 0.6f);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.GRAVEL_BREAK, SoundSource.PLAYERS, 1.2f, 0.7f);
    }

    /** Roots a creature in place for {@code ticks}. */
    static void stun(LivingEntity target, int ticks) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 6, false, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.JUMP, ticks, 128, false, false, false));
    }
}
