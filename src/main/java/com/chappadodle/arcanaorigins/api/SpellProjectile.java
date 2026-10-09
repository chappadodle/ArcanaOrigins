package com.chappadodle.arcanaorigins.api;

import com.chappadodle.arcanaorigins.content.ModMagic;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * One projectile entity for every {@link ProjectileSpell} (from Elemental Arcana, trimmed: thrown
 * the moment it's cast, never held). It syncs which spell it is, so clients draw that spell's model
 * and trail, and hands its hits back to the spell. It can pierce, bounce, fall in an arc, and wear
 * one of its spell's looks ({@link #variant}) at a scale of its own.
 */
public class SpellProjectile extends ThrowableProjectile {
    private static final EntityDataAccessor<String> SPELL = SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> GRAVITY = SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.INT);
    private static final double AIM_RANGE = 64.0;

    private float power = 1f;
    // Server-side: piercing and bouncing.
    private int pierceLeft;
    private int bouncesLeft;
    private final Set<Integer> piercedIds = new HashSet<>();
    // Client-side: noticing its first tick, for the release particles.
    private boolean released;

    public SpellProjectile(EntityType<? extends SpellProjectile> type, Level level) {
        super(type, level);
    }

    /**
     * Throws {@code spell} from the caster's right hand toward what they're aiming at (from the
     * hand, not the eyes: a projectile born at the eyes fills the caster's screen as it leaves).
     */
    public static SpellProjectile throwFor(ServerPlayer caster, ProjectileSpell spell, float speed, float power) {
        Vec3 look = caster.getLookAngle();
        Vec3 right = new Vec3(-look.z, 0, look.x);
        right = right.lengthSqr() < 1.0e-4 ? new Vec3(1, 0, 0) : right.normalize();
        Vec3 from = caster.getEyePosition().add(look.scale(0.7)).add(right.scale(0.35)).subtract(0, 0.3, 0);
        Vec3 aim = crosshairTarget(caster);
        return shootFrom(caster, spell, from, aim.subtract(from).normalize().scale(speed), power);
    }

    /** Fires a projectile of {@code spell} from any point with a given velocity (e.g. splitting off on impact). */
    public static SpellProjectile shootFrom(Entity owner, ProjectileSpell spell, Vec3 position, Vec3 velocity, float power) {
        SpellProjectile projectile = new SpellProjectile(ModMagic.SPELL_PROJECTILE.get(), owner.level());
        projectile.setOwner(owner);
        projectile.entityData.set(SPELL, spell.name());
        projectile.power = power;
        projectile.setPos(position);
        projectile.setDeltaMovement(velocity);
        owner.level().addFreshEntity(projectile);
        return projectile;
    }

    /**
     * What the caster is aiming at: the first entity or block under their crosshair, up to 64
     * blocks away (looking past anyone their magic spares, as their spells fly through them).
     */
    public static Vec3 crosshairTarget(ServerPlayer caster) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        Vec3 end = eye.add(look.scale(AIM_RANGE));
        BlockHitResult block = caster.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        Vec3 blockHit = block.getLocation();
        EntityHitResult entity = ProjectileUtil.getEntityHitResult(caster, eye, blockHit,
                caster.getBoundingBox().expandTowards(look.scale(AIM_RANGE)).inflate(1.0),
                target -> !target.isSpectator() && target.isPickable() && !SpellTargets.spares(caster, target), eye.distanceToSqr(blockHit));
        return entity != null ? entity.getEntity().getBoundingBox().getCenter() : blockHit;
    }

    /** Always 1: these projectiles are thrown at full strength (Elemental Arcana's charging is gone). */
    public float charge(float partialTick) {
        return 1f;
    }

    /** Never: kept so the ported visuals read as they did. */
    public boolean isHeld() {
        return false;
    }

    /** No first-person shrinking (nothing is held before the eyes). */
    public float viewScale() {
        return 1f;
    }

    /** Size multiplier for the projectile's model (synced). */
    public float visualScale() {
        return entityData.get(SCALE);
    }

    public void setVisualScale(float scale) {
        entityData.set(SCALE, scale);
    }

    /** Which look the spell gives this projectile (synced; 0 = the default). */
    public int variant() {
        return entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        entityData.set(VARIANT, variant);
    }

    /** Lets the projectile pass through this many more entities (each is hit only once). */
    public void setPierce(int targets) {
        pierceLeft = targets;
    }

    /** Downward pull per tick (synced, so clients predict the arc). 0 = flies straight. */
    public void setGravity(float gravity) {
        entityData.set(GRAVITY, gravity);
    }

    /** Bounces off this many blocks before a block hit counts as an impact. */
    public void setBounces(int bounces) {
        bouncesLeft = bounces;
    }

    /** Makes the projectile pass through {@code entity} without hitting it. */
    public void ignoreEntity(Entity entity) {
        piercedIds.add(entity.getId());
    }

    public int ticksInFlight() {
        return tickCount;
    }

    /**
     * Where this projectile actually touched the entity it hit. Use it instead of position() or
     * hit.getLocation() in onHitEntity: vanilla reports a hit before the projectile moves, and an
     * entity hit's location is the entity's feet, not the point of contact.
     */
    public Vec3 impactPoint(EntityHitResult hit) {
        AABB box = hit.getEntity().getBoundingBox();
        return box.inflate(0.3).clip(position(), position().add(getDeltaMovement())).orElse(box.getCenter());
    }

    public float power() {
        return power;
    }

    @Nullable
    public ProjectileSpell spell() {
        return ProjectileSpells.get(entityData.get(SPELL));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SPELL, "");
        builder.define(SCALE, 1f);
        builder.define(GRAVITY, 0f);
        builder.define(VARIANT, 0);
    }

    @Override
    protected double getDefaultGravity() {
        return entityData.get(GRAVITY);
    }

    @Override
    public void tick() {
        ProjectileSpell spell = spell();
        super.tick();
        if (level().isClientSide()) {
            if (spell != null) {
                if (!released) {
                    released = true;
                    spell.releaseParticles(this);
                }
                spell.flightParticles(this);
            }
        } else if (spell == null || tickCount > spell.lifetimeTicks()) {
            discard();
        } else if (!isRemoved()) {
            spell.onTick(this);
        }
    }

    /** Client-side helper for spell visuals: one particle at a random point within {@code spread}. */
    public void spawnParticleAround(ParticleOptions particle, double spread, Vec3 velocity) {
        level().addParticle(particle,
                getX() + (random.nextDouble() - 0.5) * 2 * spread,
                getY() + (random.nextDouble() - 0.5) * 2 * spread,
                getZ() + (random.nextDouble() - 0.5) * 2 * spread,
                velocity.x, velocity.y, velocity.z);
    }

    /** Reflects off the block face that was hit, losing some speed. */
    private void bounce(BlockHitResult hit) {
        bouncesLeft--;
        Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal());
        Vec3 velocity = getDeltaMovement();
        Vec3 reflected = velocity.subtract(normal.scale(2 * velocity.dot(normal))).scale(0.55);
        setDeltaMovement(reflected);
        setPos(hit.getLocation().add(normal.scale(0.05)));
        hasImpulse = true;
    }

    // A spell never hits its own caster, nor whom their magic spares (their pets: SpellTargets.spares);
    // it flies on through them.
    @Override
    protected boolean canHitEntity(Entity target) {
        Entity owner = getOwner();
        return super.canHitEntity(target) && target != owner && !SpellTargets.spares(owner, target) && !piercedIds.contains(target.getId());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        ProjectileSpell spell = spell();
        if (spell != null && !level().isClientSide()) {
            spell.onHitEntity(this, result);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        ProjectileSpell spell = spell();
        if (spell != null && !level().isClientSide()) {
            spell.onHitBlock(this, result);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (result instanceof BlockHitResult blockHit && bouncesLeft > 0 && !level().isClientSide()) {
            bounce(blockHit);
            return;
        }
        super.onHit(result);
        if (level().isClientSide()) {
            return;
        }
        if (result instanceof EntityHitResult entityHit && pierceLeft > 0) {
            pierceLeft--;
            piercedIds.add(entityHit.getEntity().getId());
        } else {
            discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Spell", entityData.get(SPELL));
        tag.putFloat("Power", power);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(SPELL, tag.getString("Spell"));
        power = tag.contains("Power") ? tag.getFloat("Power") : 1f;
    }
}
