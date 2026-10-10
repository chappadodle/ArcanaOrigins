package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.api.SpellTargets;
import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/**
 * Stoneborn's Tremor (Elemental Arcana's, made an ultimate): three shockwaves fan out along the
 * ground where you look, 14 blocks each at a block a tick. Each creature they reach is hit once for
 * 7, thrown up, rooted for a second and slowed (see Tremors).
 */
public class TremorSpell implements Spell {
    public static final String NAME = "tremor";
    private static final float FAN_DEGREES = 25f;

    @Override
    public boolean cast(ServerPlayer caster) {
        Vec3 look = caster.getLookAngle();
        Set<Integer> hit = new HashSet<>();
        boolean any = false;
        for (int i = -1; i <= 1; i++) {
            Vec3 direction = look.yRot(i * FAN_DEGREES * Mth.DEG_TO_RAD);
            any |= Tremors.start(caster, direction, 1f, target -> SpellTargets.canAffect(caster, target), hit);
        }
        if (!any) {
            caster.displayClientMessage(Component.translatable("message.arcanaorigins.tremor.no_ground").withStyle(ChatFormatting.GRAY), true);
        }
        return any;
    }
}
