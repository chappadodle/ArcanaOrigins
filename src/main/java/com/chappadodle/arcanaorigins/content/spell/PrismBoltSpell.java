package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.magic.Spell;

import com.chappadodle.arcanaorigins.api.Element;
import com.chappadodle.arcanaorigins.api.ElementalReactions;
import com.chappadodle.arcanaorigins.api.Glow;
import com.chappadodle.arcanaorigins.api.PrismBoltRules;
import com.chappadodle.arcanaorigins.api.ProjectileSpell;
import com.chappadodle.arcanaorigins.api.SpellDamage;
import com.chappadodle.arcanaorigins.api.SpellProjectile;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

/**
 * Prismborn's Prism Bolt (Elemental Arcana's, at one fixed strength: its level 4): a bolt of violet
 * crystal for 7.2 that bursts into five shards (3 each) on whatever it strikes, fanned on along its
 * way (off a wall, they fly back out); it passes through one creature to strike a second.
 */
public class PrismBoltSpell implements Spell, ProjectileSpell {
    public static final String NAME = "prism_bolt";
    /** A shard of a burst bolt (SpellProjectile#variant). */
    public static final int SHARD = 1;
    private static final int LEVEL = 4;
    private static final String TAG_CAST = "arcanaorigins_prism_cast";
    private static final DustParticleOptions TRAIL = new DustParticleOptions(new Vector3f(0.82f, 0.55f, 1f), 0.9f);

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public boolean cast(ServerPlayer caster) {
        SpellProjectile bolt = SpellProjectile.throwFor(caster, this, (float) PrismBoltRules.speed(null), 1f);
        bolt.setPierce(PrismBoltRules.pierce(LEVEL, null));
        bolt.getPersistentData().putInt(TAG_CAST, caster.getRandom().nextInt());
        Vec3 eye = caster.getEyePosition();
        caster.level().playSound(null, eye.x, eye.y, eye.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1f, 1.4f);
        return true;
    }

    @Override
    public ParticleOptions trailParticle() {
        return TRAIL;
    }

    @Override
    public float projectileSpeed() {
        return (float) PrismBoltRules.SPEED;
    }

    @Override
    public int lifetimeTicks() {
        return 40;
    }

    @Override
    public Glow glow(int variant) {
        return new Glow(0xD08CFF, variant == SHARD ? 0.45f : 0.9f, 0.7f);
    }

    @Override
    public void onHitEntity(SpellProjectile projectile, EntityHitResult hit) {
        if (!(projectile.level() instanceof ServerLevel level) || !(hit.getEntity() instanceof LivingEntity target)) {
            return;
        }
        Entity owner = projectile.getOwner();
        boolean shard = projectile.variant() == SHARD;
        Vec3 at = projectile.impactPoint(hit);
        float damage = (shard ? PrismBoltRules.SHARD_DAMAGE : PrismBoltRules.DAMAGE) * PrismBoltRules.damageFactor(LEVEL) * projectile.power();
        SpellDamage.hurtMultiHit(target, SpellDamage.source(level, Element.CRYSTAL, projectile, owner), damage);
        if (!shard) {
            burst(level, projectile, at, projectile.getDeltaMovement(), target);
        }
        shatter(level, at, shard);
    }

    @Override
    public void onHitBlock(SpellProjectile projectile, BlockHitResult hit) {
        if (!(projectile.level() instanceof ServerLevel level)) {
            return;
        }
        boolean shard = projectile.variant() == SHARD;
        if (!shard) {
            // Off a wall the shards fly back out: the bolt's direction, mirrored in the face it struck.
            Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal());
            Vec3 direction = projectile.getDeltaMovement();
            Vec3 reflected = direction.subtract(normal.scale(2 * direction.dot(normal)));
            Vec3 at = hit.getLocation().add(normal.scale(0.2));
            burst(level, projectile, at, reflected, null);
        }
        shatter(level, hit.getLocation(), shard);
    }

    /**
     * The burst: shards from {@code at}, fanned around {@code direction} (or all the way round, for a
     * Geode Burst), sparing what the bolt just struck.
     */
    private void burst(ServerLevel level, SpellProjectile bolt, Vec3 at, Vec3 direction, @Nullable Entity spare) {
        Entity owner = bolt.getOwner();
        if (owner == null || direction.lengthSqr() < 1.0e-6) {
            return;
        }
        int count = PrismBoltRules.shards(LEVEL, null);
        float spread = (float) Math.toRadians(PrismBoltRules.spreadDegrees(LEVEL, null));
        Vec3 forward = direction.normalize();
        for (int i = 0; i < count; i++) {
            Vec3 velocity = forward.yRot(spread * (i - (count - 1) / 2f)).normalize().scale(1.3);
            shard(owner, bolt, at, velocity, spare, 0.6f);
        }
    }

    /** One shard of a burst. */
    private void shard(Entity owner, SpellProjectile parent, Vec3 at, Vec3 velocity, @Nullable Entity spare, float scale) {
        SpellProjectile shard = SpellProjectile.shootFrom(owner, this, at, velocity, parent.power());
        shard.setVariant(SHARD);
        shard.setVisualScale(scale);
        if (spare != null) {
            shard.ignoreEntity(spare);
        }
    }

    private static void shatter(ServerLevel level, Vec3 at, boolean shard) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_BLOCK.defaultBlockState()),
                at.x, at.y, at.z, shard ? 6 : 16, 0.15, 0.15, 0.15, 0.15);
        level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, shard ? 1 : 5, 0.1, 0.1, 0.1, 0.06);
        level.playSound(null, at.x, at.y, at.z, shard ? SoundEvents.AMETHYST_BLOCK_HIT : SoundEvents.AMETHYST_CLUSTER_BREAK,
                SoundSource.PLAYERS, shard ? 0.5f : 1f, shard ? 1.6f : 1.1f);
    }
}
