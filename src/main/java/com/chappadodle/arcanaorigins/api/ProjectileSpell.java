package com.chappadodle.arcanaorigins.api;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A spell that flies as a {@link SpellProjectile} (from Elemental Arcana, trimmed: no held or
 * conjured projectiles, no levels). Registered by name in {@link ProjectileSpells}, so a projectile
 * finds its spell again on both sides.
 */
public interface ProjectileSpell {
    /** Its name, as {@link ProjectileSpells} knows it (and as the projectile syncs it). */
    String name();

    ParticleOptions trailParticle();

    default float projectileSpeed() {
        return 1.6f;
    }

    /** Ticks in flight before it fizzles out. */
    default int lifetimeTicks() {
        return 60;
    }

    /** The 3D model drawn for the projectile (a standalone model id), or null for none. */
    @Nullable
    default ResourceLocation model() {
        return null;
    }

    /** The model for one of its looks ({@link SpellProjectile#variant}). */
    @Nullable
    default ResourceLocation model(int variant) {
        return model();
    }

    /** Every model it uses, for the client to load. */
    default List<ResourceLocation> models() {
        return model() == null ? List.of() : List.of(model());
    }

    /** A soft halo of light round the projectile, or null for none. */
    @Nullable
    default Glow glow(int variant) {
        return null;
    }

    // ---- client-side visuals ----

    default void flightParticles(SpellProjectile projectile) {
        for (int i = 0; i < 3; i++) {
            projectile.spawnParticleAround(trailParticle(), 0.1, Vec3.ZERO);
        }
    }

    /** Its first tick in flight on a client (a burst as it leaves the hand). */
    default void releaseParticles(SpellProjectile projectile) {
    }

    // ---- server-side hooks ----

    default void onTick(SpellProjectile projectile) {
    }

    default void onHitEntity(SpellProjectile projectile, EntityHitResult hit) {
    }

    default void onHitBlock(SpellProjectile projectile, BlockHitResult hit) {
    }
}
