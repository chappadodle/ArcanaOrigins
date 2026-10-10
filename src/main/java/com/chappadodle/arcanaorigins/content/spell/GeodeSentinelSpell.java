package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Prismborn's Geode Sentinel (Elemental Arcana's): a crystal sentinel grows where the caster looks
 * (up to 16 blocks, pulled back off a wall) and draws the foes round it onto itself for 12 seconds
 * (see GeodeSentinel).
 */
public class GeodeSentinelSpell implements Spell {
    public static final String NAME = "geode_sentinel";
    private static final double RANGE = 16;

    @Override
    public boolean cast(ServerPlayer caster) {
        Vec3 eye = caster.getEyePosition();
        Vec3 reach = eye.add(caster.getLookAngle().scale(RANGE));
        BlockHitResult hit = caster.level().clip(new ClipContext(eye, reach, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        Vec3 at = hit.getType() == HitResult.Type.BLOCK
                ? hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(0.5))
                : reach;
        GeodeSentinel.grow(caster.serverLevel(), caster, at, 1f);
        return true;
    }
}
