package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.api.StormeyeRules;
import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Galeborn's Stormeye (Elemental Arcana's, at one fixed strength: its level 7): a tornado set down
 * where you look, up to 20 blocks away. For 5 seconds it drags in everything within 7 blocks, lifts
 * it and whirls it round, hitting each for 2.25 every half second; it takes on the element of the
 * first creature it holds that carries one, and spreads it to the rest (Swirl).
 */
public class StormeyeSpell implements Spell {
    public static final String NAME = "stormeye";
    private static final int LEVEL = 7;

    @Override
    public boolean cast(ServerPlayer caster) {
        Vec3 at = groundUnderAim(caster.serverLevel(), caster);
        if (at == null) {
            caster.displayClientMessage(Component.translatable("message.arcanaorigins.stormeye.no_ground").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        StormeyeEntity.raise(caster.serverLevel(), caster, at, LEVEL, null, null, 1f);
        return true;
    }

    /** The ground under where {@code caster} looks, up to 20 blocks away (or as far as they can see), or null. */
    @Nullable
    public static Vec3 groundUnderAim(ServerLevel level, LivingEntity caster) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        Vec3 end = eye.add(look.scale(StormeyeRules.RANGE));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, caster));
        Vec3 point = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation().subtract(look.scale(0.3));
        return groundBelow(level, point.add(0, 0.5, 0), 24);
    }

    /** The top of the first ground (or water) at or below {@code from}, at most {@code depth} blocks down, or null. */
    @Nullable
    public static Vec3 groundBelow(ServerLevel level, Vec3 from, int depth) {
        BlockPos start = BlockPos.containing(from);
        for (int down = 0; down <= depth; down++) {
            BlockPos pos = start.below(down);
            BlockState state = level.getBlockState(pos);
            if (!state.getCollisionShape(level, pos).isEmpty() || !state.getFluidState().isEmpty()) {
                return new Vec3(from.x, pos.getY() + 1, from.z);
            }
        }
        return null;
    }
}
