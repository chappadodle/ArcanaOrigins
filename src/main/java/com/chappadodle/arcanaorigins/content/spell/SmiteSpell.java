package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.server.level.ServerPlayer;

import com.chappadodle.arcanaorigins.api.SmiteRules;
import com.chappadodle.arcanaorigins.api.SpellTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Dawnborn's Smite (Elemental Arcana's, at one fixed strength: its level 4): marks the ground where
 * the caster looks (up to 24 blocks, or the feet of a creature they look at); half a second later a
 * pillar of light strikes it (see Smites), hurting everything within 3.5 blocks, the undead most,
 * setting them alight and making them glow, and leaving holy ground for 4 seconds.
 */
public class SmiteSpell implements Spell {
    public static final String NAME = "smite";
    private static final int LEVEL = 4;
    private static final double AIM_LEEWAY = 0.5;

    @Override
    public boolean cast(ServerPlayer caster) {
        ServerLevel level = caster.serverLevel();
        LivingEntity aimed = SpellTargets.underCrosshair(caster, SmiteRules.RANGE, AIM_LEEWAY, target -> true);
        Vec3 at = aimed != null ? aimed.position() : groundAhead(level, caster);
        Smites.strike(level, at, caster, 1f, Smites.Judgment.of(LEVEL), SmiteRules.delay(LEVEL), 0,
                target -> SpellTargets.canAffect(caster, target), null);
        return true;
    }

    /** Where the caster's look meets a block (or, at the end of range, the ground under that point). */
    private static Vec3 groundAhead(ServerLevel level, LivingEntity caster) {
        Vec3 eye = caster.getEyePosition();
        Vec3 end = eye.add(caster.getLookAngle().scale(SmiteRules.RANGE));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        if (hit.getType() == HitResult.Type.BLOCK) {
            return hit.getLocation();
        }
        BlockPos pos = BlockPos.containing(end);
        for (int down = 0; down < 24 && level.isEmptyBlock(pos.below()); down++) {
            pos = pos.below();
        }
        return Vec3.atBottomCenterOf(pos);
    }
}
