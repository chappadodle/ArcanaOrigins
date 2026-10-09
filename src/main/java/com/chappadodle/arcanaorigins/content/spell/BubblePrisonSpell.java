package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.magic.Spell;
import com.chappadodle.arcanaorigins.api.SpellTargets;
import com.chappadodle.arcanaorigins.content.BubblePrisons;
import com.chappadodle.arcanaorigins.content.ModMagic;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Tideborn's Bubble Prison (Elemental Arcana's): traps the creature under your crosshair (up to 20
 * blocks) in a floating water bubble for 4 seconds. It hangs there helpless and Wet; any hit from a
 * player pops it for bonus damage (see BubblePrisons). No target: nothing happens, and no cooldown
 * is taken.
 */
public class BubblePrisonSpell implements Spell {
    private static final double RANGE = 20;
    // A little leeway around creatures, so small ones are easy to target.
    private static final double AIM_LEEWAY = 0.5;

    @Override
    public boolean cast(ServerPlayer caster) {
        LivingEntity target = targetUnderCrosshair(caster);
        if (target == null) {
            caster.displayClientMessage(Component.translatable("message.arcanaorigins.bubble.no_target"), true);
            return false;
        }
        BubblePrisons.trap(target);
        ServerLevel level = caster.serverLevel();
        Vec3 from = caster.getEyePosition();
        Vec3 to = target.getBoundingBox().getCenter();
        // A line of little water cubes zipping from the caster to the target (see BubbleCastEmitter).
        Vec3 start = from.add(to.subtract(from).normalize().scale(Math.min(0.8, from.distanceTo(to) / 2)));
        Vec3 line = to.subtract(start);
        level.sendParticles(ModMagic.BUBBLE_CAST.get(), start.x, start.y, start.z, 0, line.x, line.y, line.z, 1.0);
        return true;
    }

    /** The nearest creature the caster is looking at, in sight and trappable (not one their magic spares), or null. */
    @Nullable
    private static LivingEntity targetUnderCrosshair(ServerPlayer caster) {
        return SpellTargets.underCrosshair(caster, RANGE, AIM_LEEWAY,
                target -> BubblePrisons.canTrap(target) && !SpellTargets.spares(caster, target));
    }
}
