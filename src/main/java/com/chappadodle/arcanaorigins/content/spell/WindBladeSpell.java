package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.api.Element;
import com.chappadodle.arcanaorigins.api.ElementalReactions;
import com.chappadodle.arcanaorigins.api.ProjectileSpell;
import com.chappadodle.arcanaorigins.api.SpellDamage;
import com.chappadodle.arcanaorigins.api.SpellProjectile;
import com.chappadodle.arcanaorigins.client.sound.WindBladeSounds;
import com.chappadodle.arcanaorigins.content.ModMagic;
import com.chappadodle.arcanaorigins.content.WindCutOptions;
import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Galeborn's Wind Blade (Elemental Arcana's, at one fixed strength): a crescent of keen wind slashed
 * toward the crosshair the moment it's cast. It cuts through one foe and on into a second, knocks
 * back what it cuts, and Swirls (spreads the target's element to creatures nearby). It's air: it
 * thins out after about 15 blocks.
 */
public class WindBladeSpell implements Spell, ProjectileSpell {
    public static final String NAME = "wind_blade";

    // Elemental Arcana's looks (SpellProjectile#variant), drawn by client/visual/WindSlashRenderer;
    // kept so its visuals read as they did. Arcana Origins slashes the Gale look.
    public static final int LOOK_GUST = 0;
    public static final int LOOK_BREEZE = 1;
    public static final int LOOK_GALE = 2;
    public static final int LOOK_TEMPEST = 3;
    public static final int LOOK_BOOMERANG = 4;
    public static final int LOOK_TEMPEST_EDGE = 5;
    public static final int LOOK_THOUSAND_CUTS = 6;
    public static final int LOOK_SCYTHE = 7;
    /** Added to a look: the edge shines brighter. */
    public static final int BRIGHT = 8;
    private static final int LOOK = LOOK_GALE;

    private static final float SPEED = 2.8f;
    private static final int RANGE_TICKS = 20;
    private static final float DAMAGE = 6f;
    private static final double KNOCKBACK = 1.0;

    /** A look without its brightness flag. */
    public static int look(int variant) {
        return variant & (BRIGHT - 1);
    }

    /**
     * A fanned set's fixed tilts, in radians (Elemental Arcana's volleys; a single blade is level).
     */
    public static float fanAngle(int slot, int count) {
        if (count <= 1) {
            return 0f;
        }
        float spread = count >= 5 ? 60f : count >= 3 ? 40f : 30f;
        return Mth.DEG_TO_RAD * Mth.lerp(slot / (float) (count - 1), -spread, spread);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public boolean cast(ServerPlayer caster) {
        SpellProjectile blade = SpellProjectile.throwFor(caster, this, SPEED, 1f);
        blade.setVariant(LOOK);
        blade.setPierce(1);
        return true;
    }

    @Override
    public ParticleOptions trailParticle() {
        return ModMagic.WIND_STREAK.get();
    }

    @Override
    public int lifetimeTicks() {
        return RANGE_TICKS + 10;
    }

    // ---- visuals and sounds (client only, see WindBladeEffects and WindBladeSounds) ----

    @Override
    public void releaseParticles(SpellProjectile blade) {
        WindBladeEffects.released(blade);
    }

    @Override
    public void flightParticles(SpellProjectile blade) {
        WindBladeEffects.flight(blade);
        WindBladeSounds.flight(blade);
    }

    // ---- flight and hits (server) ----

    @Override
    public void onTick(SpellProjectile blade) {
        if (blade.ticksInFlight() > RANGE_TICKS) {
            impact(blade, blade.position());
            blade.discard();
        }
    }

    @Override
    public void onHitEntity(SpellProjectile blade, EntityHitResult hit) {
        if (!(hit.getEntity() instanceof LivingEntity target)) {
            return;
        }
        Entity owner = blade.getOwner();
        SpellDamage.hurtMultiHit(target, SpellDamage.source(blade.level(), Element.WIND, blade, owner), DAMAGE * blade.power());
        Vec3 direction = blade.getDeltaMovement().normalize();
        target.knockback(KNOCKBACK, -direction.x, -direction.z);
        target.hurtMarked = true;
        ElementalReactions.swirl(target, owner, blade.power());
        impact(blade, blade.impactPoint(hit));
    }

    @Override
    public void onHitBlock(SpellProjectile blade, BlockHitResult hit) {
        impact(blade, hit.getLocation());
    }

    /** Where it strikes: the whole impact goes out as one particle; each client plays it out with its sound. */
    private static void impact(SpellProjectile blade, Vec3 at) {
        ServerLevel level = (ServerLevel) blade.level();
        Vec3 direction = blade.getDeltaMovement().lengthSqr() > 1.0e-6 ? blade.getDeltaMovement().normalize() : new Vec3(0, 0, 1);
        WindCutOptions cut = WindCutOptions.of(blade.variant(), blade.visualScale(), 1f, direction, 0f, false);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(at) < 32 * 32) {
                level.sendParticles(player, cut, true, at.x, at.y, at.z, 1, 0, 0, 0, 0);
            }
        }
    }
}
