package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.ChatFormatting;

import com.chappadodle.arcanaorigins.api.ChainLightningRules;
import com.chappadodle.arcanaorigins.api.SpellTargets;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

/**
 * Stormborn's Chain Lightning (Elemental Arcana's, at one fixed strength: its level 4): lightning
 * leaps from your hand to the creature under your crosshair, up to 26 blocks away, then on from
 * creature to creature up to 8 blocks apart, four jumps, each strike a little weaker. A soaked
 * creature conducts it: struck half again as hard, and the chain gains two jumps. Every strike
 * jolts (slowed and weakened for half a second).
 */
public class ChainLightningSpell implements Spell {
    public static final String NAME = "chain_lightning";
    private static final int LEVEL = 4;
    private static final double AIM_LEEWAY = 0.6;
    /** The caster's own bolt is drawn from this far past the hand, so nothing balloons in front of their eyes. */
    static final double FIRST_PERSON_SKIP = 1.0;

    @Override
    public boolean cast(ServerPlayer caster) {
        LivingEntity first = aim(caster);
        if (first == null) {
            caster.displayClientMessage(Component.translatable("message.arcanaorigins.chain_lightning.no_target").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        ChainLightnings.chain(caster.serverLevel(), caster, hand(caster), FIRST_PERSON_SKIP, first, 1f,
                ChainLightnings.Chain.of(LEVEL, null), target -> SpellTargets.canAffect(caster, target));
        ChainLightnings.thunder(caster.serverLevel(), caster);
        return true;
    }

    /** The creature under the caster's crosshair within the first strike's reach, or null. */
    @Nullable
    static LivingEntity aim(LivingEntity caster) {
        return SpellTargets.underCrosshair(caster, ChainLightningRules.range(LEVEL), AIM_LEEWAY,
                target -> SpellTargets.canAffect(caster, target));
    }

    /** Where the bolt leaves a caster: a little in front of the right hand. */
    public static Vec3 hand(LivingEntity caster) {
        float yaw = caster.getYRot() * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Mth.cos(yaw), 0, -Mth.sin(yaw));
        return caster.getEyePosition().add(caster.getLookAngle().scale(0.6)).add(right.scale(0.3)).add(0, -0.3, 0);
    }

    /** A bolt from {@code a} to {@code b}, none of the first {@code skip} blocks drawn. */
    public static void bolt(ServerLevel level, Vec3 a, Vec3 b, double skip) {
        ChainLightnings.bolt(level, a, b, skip, 1f);
    }
}
