package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Emberborn's Ember Sprite (Elemental Arcana's): a little fire spirit set where you look (up to 20 blocks), spitting bolts at foes. */
public class EmberSpriteSpell implements Spell {
    private static final double RANGE = 20;

    @Override
    public boolean cast(ServerPlayer caster) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        BlockHitResult hit = caster.level().clip(new ClipContext(eye, eye.add(look.scale(RANGE)), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, caster));
        Vec3 at = hit.getType() == HitResult.Type.MISS ? eye.add(look.scale(RANGE))
                : hit.getLocation().subtract(look.scale(0.8)).add(0, 1.0, 0);
        EmberSprite.place(caster.serverLevel(), caster, at, 1f);
        return true;
    }
}
