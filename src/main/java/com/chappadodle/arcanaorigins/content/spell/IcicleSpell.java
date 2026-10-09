package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.ArcanaOrigins;
import com.chappadodle.arcanaorigins.api.Element;
import com.chappadodle.arcanaorigins.api.ElementalReactions;
import com.chappadodle.arcanaorigins.api.Glow;
import com.chappadodle.arcanaorigins.api.ProjectileSpell;
import com.chappadodle.arcanaorigins.api.SpellDamage;
import com.chappadodle.arcanaorigins.api.SpellProjectile;
import com.chappadodle.arcanaorigins.client.sound.IcicleSounds;
import com.chappadodle.arcanaorigins.content.IceShatterOptions;
import com.chappadodle.arcanaorigins.content.ModMagic;
import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Rimeborn's Icicle (Elemental Arcana's, at one fixed strength): a fast, cold icicle thrown the
 * moment it's cast. It pierces one foe and flies on to a second; each it hits is frosted over (the
 * vanilla freeze) and slowed, and a wet one Freezes solid. Where it lands, water nearby freezes over.
 */
public class IcicleSpell implements Spell, ProjectileSpell {
    public static final String NAME = "icicle";

    // Elemental Arcana's looks (SpellProjectile#variant), kept so its icicle visuals read as they did;
    // Arcana Origins throws the third frost stage.
    public static final int LOOK_FROST_1 = 0;
    public static final int LOOK_FROST_2 = 1;
    public static final int LOOK_FROST_3 = 2;
    public static final int LOOK_FROST_4 = 3;
    public static final int LOOK_PIERCING = 4;
    public static final int LOOK_SHATTER = 5;
    public static final int LOOK_WINTER = 6;
    public static final int LOOK_LANCE = 7;
    public static final int BRIGHT = 8;
    private static final int LOOK = LOOK_FROST_3;
    private static final ResourceLocation MODEL = ArcanaOrigins.id("spell/icicle_frost3");
    private static final Glow GLOW = new Glow(0x5AA8FF, 1.0f, 0.45f);

    private static final float SPEED = 2.4f;
    private static final float DAMAGE = 6f;
    private static final int SLOW_TICKS = 70;
    private static final int FROST_TICKS = 100;
    private static final int FREEZE_WATER_RADIUS = 2;

    /** A look without its brightness flag (Elemental Arcana's: +8 at Lv 8 and up). */
    public static int look(int variant) {
        return variant & (BRIGHT - 1);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public boolean cast(ServerPlayer caster) {
        SpellProjectile icicle = SpellProjectile.throwFor(caster, this, SPEED, 1f);
        icicle.setVariant(LOOK);
        icicle.setPierce(1);
        return true;
    }

    @Override
    public ParticleOptions trailParticle() {
        return ModMagic.FROST_SPARKLE.get();
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
        return 60;
    }

    // ---- visuals and sounds (client only, see IcicleEffects and IcicleSounds) ----

    @Override
    public void releaseParticles(SpellProjectile icicle) {
        IcicleEffects.released(icicle);
    }

    @Override
    public void flightParticles(SpellProjectile icicle) {
        IcicleEffects.flight(icicle);
        IcicleSounds.flight(icicle);
    }

    // ---- hits (server) ----

    @Override
    public void onHitEntity(SpellProjectile icicle, EntityHitResult hit) {
        Entity target = hit.getEntity();
        SpellDamage.hurtMultiHit(target, SpellDamage.source(icicle.level(), Element.ICE, icicle, icicle.getOwner()), DAMAGE * icicle.power());
        if (target instanceof LivingEntity living) {
            ElementalReactions.iceHit(living);
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_TICKS, 1));
        }
        if (target.canFreeze()) {
            freezeOver(target, FROST_TICKS);
        }
        shatter(icicle, icicle.impactPoint(hit));
    }

    @Override
    public void onHitBlock(SpellProjectile icicle, BlockHitResult hit) {
        Freezing.freezeWater((ServerLevel) icicle.level(), hit.getBlockPos(), FREEZE_WATER_RADIUS);
        shatter(icicle, hit.getLocation());
    }

    /** The target frosts over (vanilla freeze overlay and shivering) with a crust of ice. */
    private static void freezeOver(Entity target, int extraTicks) {
        target.setTicksFrozen(Math.max(target.getTicksFrozen(), target.getTicksRequiredToFreeze() + extraTicks));
        ServerLevel level = (ServerLevel) target.level();
        double width = target.getBbWidth() * 0.6;
        double height = target.getBbHeight() * 0.5;
        level.sendParticles(ModMagic.FROST_SPARKLE.get(), target.getX(), target.getY(0.5), target.getZ(), 18, width, height, width, 0.02);
        level.sendParticles(ModMagic.FROST_MIST.get(), target.getX(), target.getY(0.3), target.getZ(), 5, width * 0.6, height * 0.5, width * 0.6, 0.01);
        level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.5), target.getZ(), 12, width, height, width, 0.01);
    }

    /** Shatter: shards spray and rain down, a puff of cold mist, glints (one particle; each client plays it out). */
    private static void shatter(SpellProjectile icicle, Vec3 at) {
        ServerLevel level = (ServerLevel) icicle.level();
        IceShatterOptions shatter = new IceShatterOptions(icicle.variant(), icicle.visualScale(), 1f);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(at.x, at.y, at.z) < 32 * 32) {
                level.sendParticles(player, shatter, true, at.x, at.y, at.z, 1, 0, 0, 0, 0);
            }
        }
    }
}
