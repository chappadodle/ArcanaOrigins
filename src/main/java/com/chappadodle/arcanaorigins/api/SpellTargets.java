package com.chappadodle.arcanaorigins.api;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Who a spell may hurt, given who cast it (from Elemental Arcana, for Origins servers). A player's
 * magic never touches their own pets, and its area effects (bursts, burning ground, wheels of
 * flame) spare villagers; other players are fair game only where the server allows PvP. A
 * creature's magic (an Ember Sprite's bolts, for instance) is its owner's. The caster is never
 * affected. Also: whom a spell spares even with a direct hit, and which creature a caster aims at.
 */
public final class SpellTargets {
    private SpellTargets() {
    }

    /** Whether an area effect cast by {@code owner} may touch {@code target}. */
    public static boolean canAffect(@Nullable Entity owner, LivingEntity target) {
        if (target == owner || !target.isAlive() || spares(owner, target)) {
            return false;
        }
        if (owner instanceof OwnableEntity pet && pet.getOwner() instanceof Player master) {
            return canAffect(master, target);
        }
        if (owner instanceof Player) {
            if (target instanceof AbstractVillager) {
                return false;
            }
            return !(target instanceof Player) || pvp(owner);
        }
        return true;
    }

    /** Whom a spell never touches, even with a direct hit: a player's own pets (and a pet's master and fellow pets). */
    public static boolean spares(@Nullable Entity owner, Entity target) {
        if (owner instanceof OwnableEntity pet && pet.getOwner() instanceof Player master) {
            return target == master || spares(master, target);
        }
        if (owner instanceof Player player) {
            return target instanceof OwnableEntity pet && player.getUUID().equals(pet.getOwnerUUID())
                    || target instanceof Player && !pvp(owner);
        }
        return false;
    }

    private static boolean pvp(Entity owner) {
        MinecraftServer server = owner.getServer();
        return server == null || server.isPvpAllowed();
    }

    /**
     * The nearest creature {@code caster} is looking at within {@code range} blocks, in sight (walls
     * stop the aim), that {@code allowed} accepts, or null. {@code leeway} widens every creature's box
     * a little, so small ones are easy to aim at.
     */
    @Nullable
    public static LivingEntity underCrosshair(LivingEntity caster, double range, double leeway, Predicate<LivingEntity> allowed) {
        Vec3 eye = caster.getEyePosition();
        Vec3 end = eye.add(caster.getLookAngle().scale(range));
        BlockHitResult wall = caster.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        if (wall.getType() != HitResult.Type.MISS) {
            end = wall.getLocation();
        }
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity entity : caster.level().getEntitiesOfClass(LivingEntity.class, new AABB(eye, end).inflate(leeway + 1),
                e -> e != caster && !e.isSpectator() && allowed.test(e))) {
            Optional<Vec3> hit = entity.getBoundingBox().inflate(leeway).clip(eye, end);
            if (hit.isPresent() && hit.get().distanceToSqr(eye) < bestDistance) {
                best = entity;
                bestDistance = hit.get().distanceToSqr(eye);
            }
        }
        return best;
    }
}
