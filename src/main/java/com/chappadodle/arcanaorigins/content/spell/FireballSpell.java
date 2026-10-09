package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.ArcanaOrigins;
import com.chappadodle.arcanaorigins.api.Element;
import com.chappadodle.arcanaorigins.api.ElementalReactions;
import com.chappadodle.arcanaorigins.api.Glow;
import com.chappadodle.arcanaorigins.api.ProjectileSpell;
import com.chappadodle.arcanaorigins.api.SpellDamage;
import com.chappadodle.arcanaorigins.api.SpellProjectile;
import com.chappadodle.arcanaorigins.api.SpellTargets;
import com.chappadodle.arcanaorigins.client.sound.FireballSounds;
import com.chappadodle.arcanaorigins.content.FireBlastOptions;
import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Emberborn's Fireball (Elemental Arcana's, at one fixed strength): thrown the moment it's cast, it
 * bursts where it lands in a 2.5-block blast that burns and shoves everything in it, sets the
 * block it hits alight (where mob griefing is on), Melts the frozen and Vaporizes the wet. It
 * fizzles out in water.
 */
public class FireballSpell implements Spell, ProjectileSpell {
    public static final String NAME = "fireball";

    // Elemental Arcana's looks (SpellProjectile#variant), kept so its fireball visuals read as they
    // did; Arcana Origins throws the third heat stage, a bright clean flame.
    public static final int LOOK_HEAT_1 = 0;
    public static final int LOOK_HEAT_2 = 1;
    public static final int LOOK_HEAT_3 = 2;
    public static final int LOOK_HEAT_4 = 3;
    public static final int LOOK_CLUSTER = 4;
    public static final int LOOK_METEOR = 5;
    public static final int LOOK_SUN = 6;
    public static final int LOOK_PHOENIX = 7;
    private static final int LOOK = LOOK_HEAT_3;
    private static final ResourceLocation MODEL = ArcanaOrigins.id("spell/fireball_heat3");
    private static final Glow GLOW = new Glow(0xFFB446, 1.4f, 0.85f);

    private static final float SPEED = 1.5f;
    private static final double RADIUS = 2.5;
    private static final float DAMAGE = 7f;
    private static final int BURN_TICKS = 100;
    private static final double KNOCKBACK = 0.4;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public boolean cast(ServerPlayer caster) {
        SpellProjectile fireball = SpellProjectile.throwFor(caster, this, SPEED, 1f);
        fireball.setVariant(LOOK);
        return true;
    }

    @Override
    public ParticleOptions trailParticle() {
        return ParticleTypes.FLAME;
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
    public Glow glow(int variant) {
        return GLOW;
    }

    @Override
    public int lifetimeTicks() {
        return 80;
    }

    // ---- visuals and sounds (client only, see FireballEffects and FireballSounds) ----

    @Override
    public void releaseParticles(SpellProjectile fireball) {
        FireballEffects.released(fireball);
    }

    @Override
    public void flightParticles(SpellProjectile fireball) {
        FireballEffects.flight(fireball);
        FireballSounds.flight(fireball);
    }

    // ---- flight and impact (server) ----

    @Override
    public void onTick(SpellProjectile fireball) {
        if (fireball.isInWater()) {
            // Fire doesn't survive water: it fizzles out in a puff of steam.
            ServerLevel level = (ServerLevel) fireball.level();
            level.sendParticles(ParticleTypes.CLOUD, fireball.getX(), fireball.getY(), fireball.getZ(), 8, 0.2, 0.2, 0.2, 0.03);
            level.playSound(null, fireball.getX(), fireball.getY(), fireball.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.8f, 1.2f);
            fireball.discard();
        }
    }

    @Override
    public void onHitEntity(SpellProjectile fireball, EntityHitResult hit) {
        explode(fireball, hit.getEntity(), fireball.impactPoint(hit));
    }

    @Override
    public void onHitBlock(SpellProjectile fireball, BlockHitResult hit) {
        explode(fireball, null, hit.getLocation());
        ServerLevel level = (ServerLevel) fireball.level();
        BlockPos firePos = hit.getBlockPos().relative(hit.getDirection());
        if (level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && level.isEmptyBlock(firePos)
                && BaseFireBlock.canBePlacedAt(level, firePos, hit.getDirection())) {
            level.setBlockAndUpdate(firePos, BaseFireBlock.getState(level, firePos));
        }
    }

    /** The blast: burns and shoves everything within its radius (the creature it hit takes it all); Melts and Vaporizes. */
    private static void explode(SpellProjectile fireball, @Nullable Entity directHit, Vec3 at) {
        ServerLevel level = (ServerLevel) fireball.level();
        Entity owner = fireball.getOwner();
        float damage = DAMAGE * fireball.power();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(RADIUS),
                e -> e != owner && e.isAlive() && (e == directHit || SpellTargets.canAffect(owner, e)
                        && e.getBoundingBox().getCenter().distanceTo(at) <= RADIUS + e.getBbWidth() / 2))) {
            double falloff = target == directHit ? 1.0
                    : 1.0 - 0.5 * Math.min(1.0, target.getBoundingBox().getCenter().distanceTo(at) / RADIUS);
            float hit = (float) (damage * falloff) * ElementalReactions.fireHit(target);
            SpellDamage.hurtMultiHit(target, SpellDamage.source(level, Element.FIRE, fireball, owner), hit);
            target.igniteForTicks(BURN_TICKS);
            Vec3 away = target.position().subtract(at).normalize();
            target.knockback(KNOCKBACK, -away.x, -away.z);
            target.hurtMarked = true;
        }
        // The whole explosion goes out as one particle; each client plays it out, with its sound.
        FireBlastOptions blast = new FireBlastOptions(fireball.variant(), (float) RADIUS, false);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(at) < 32 * 32) {
                level.sendParticles(player, blast, true, at.x, at.y, at.z, 1, 0, 0, 0, 0);
            }
        }
    }
}
