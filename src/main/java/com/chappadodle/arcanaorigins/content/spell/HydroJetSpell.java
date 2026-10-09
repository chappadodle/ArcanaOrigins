package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.ArcanaOrigins;
import com.chappadodle.arcanaorigins.api.CreatureElements;
import com.chappadodle.arcanaorigins.api.Element;
import com.chappadodle.arcanaorigins.api.ElementalReactions;
import com.chappadodle.arcanaorigins.api.SpellDamage;
import com.chappadodle.arcanaorigins.api.SpellTargets;
import com.chappadodle.arcanaorigins.content.HydroStreamOptions;
import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Tideborn's Hydro Jet (Elemental Arcana's, at one fixed strength): a tap sprays a stream of water
 * from your hand for a second and a half, following your aim, 16 blocks out. It stops at the first
 * creature it meets, soaks it (Wet: set up for Freeze, and Vaporize when it burns), pushes it back,
 * and splashes whoever's close by; it puts out fires where it lands. Water-sensitive creatures
 * without an element (endermen) take double.
 */
@EventBusSubscriber(modid = ArcanaOrigins.MODID)
public class HydroJetSpell implements Spell {
    // Elemental Arcana's looks, kept so its stream visuals read as they did; Arcana Origins sprays the Surge.
    public static final int LOOK_SPRING = 0;
    public static final int LOOK_CURRENT = 1;
    public static final int LOOK_SURGE = 2;
    public static final int LOOK_DELUGE = 3;
    public static final int LOOK_TIDECUTTER = 4;
    public static final int LOOK_TORRENT = 5;
    public static final int LOOK_MAELSTROM = 6;
    public static final int LOOK_LANCE = 7;
    public static final int BRIGHT = 8;
    private static final int LOOK = LOOK_SURGE;

    private static final int SPRAY_TICKS = 30;
    private static final double RANGE = 16;
    private static final int HIT_INTERVAL = 4;
    private static final float HIT_DAMAGE = 1.5f;
    private static final int WET_TICKS = 100;
    private static final double PUSH = 0.15;
    private static final Map<UUID, Integer> SPRAYING = new HashMap<>();

    /** A look without its brightness flag. */
    public static int look(int variant) {
        return variant & (BRIGHT - 1);
    }

    public static Vec3 streamOrigin(Player player) {
        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Mth.cos(yaw), 0, -Mth.sin(yaw));
        return player.getEyePosition().add(player.getLookAngle().scale(0.5)).add(right.scale(0.3)).add(0, -0.2, 0);
    }

    @Override
    public boolean cast(ServerPlayer caster) {
        SPRAYING.put(caster.getUUID(), 0);
        caster.serverLevel().playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.9f, 1.3f);
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (Iterator<Map.Entry<UUID, Integer>> it = SPRAYING.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Integer> entry = it.next();
            ServerPlayer caster = event.getServer().getPlayerList().getPlayer(entry.getKey());
            int ticks = entry.getValue();
            if (caster == null || !caster.isAlive() || ticks >= SPRAY_TICKS) {
                it.remove();
                continue;
            }
            entry.setValue(ticks + 1);
            spray(caster, ticks);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        SPRAYING.clear();
    }

    /** A creature the stream passes through, and where. */
    private record LineHit(LivingEntity entity, Vec3 at) {
    }

    private static void spray(ServerPlayer caster, int ticks) {
        ServerLevel level = caster.serverLevel();
        Vec3 look = caster.getLookAngle();
        Vec3 origin = streamOrigin(caster);
        Vec3 end = origin.add(look.scale(RANGE));
        BlockHitResult blockHit = level.clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, caster));
        Vec3 stop = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();
        // The stream stops at the first creature it meets.
        LineHit first = firstOnLine(caster, origin, stop);
        if (first != null) {
            stop = first.at();
        }
        HydroStreamOptions stream = new HydroStreamOptions(caster.getId(), LOOK, 0f);
        Vec3 line = stop.subtract(origin);
        level.sendParticles(stream, origin.x, origin.y, origin.z, 0, line.x, line.y, line.z, 1.0);
        if (ticks % HIT_INTERVAL != 0) {
            return;
        }
        List<LivingEntity> hit = new ArrayList<>();
        if (first != null) {
            hitCreature(caster, first.entity(), look);
            hit.add(first.entity());
        }
        splash(caster, stop, hit);
        if (first == null && blockHit.getType() == HitResult.Type.BLOCK) {
            douse(level, blockHit);
        }
    }

    private static LineHit firstOnLine(ServerPlayer caster, Vec3 from, Vec3 to) {
        List<LineHit> hits = new ArrayList<>();
        for (LivingEntity entity : caster.level().getEntitiesOfClass(LivingEntity.class, new AABB(from, to).inflate(0.8),
                e -> e != caster && e.isAlive() && !e.isSpectator() && !SpellTargets.spares(caster, e))) {
            Optional<Vec3> at = entity.getBoundingBox().inflate(0.3).clip(from, to);
            at.ifPresent(point -> hits.add(new LineHit(entity, point)));
        }
        return hits.stream().min(Comparator.comparingDouble(hit -> hit.at().distanceToSqr(from))).orElse(null);
    }

    private static void hitCreature(ServerPlayer caster, LivingEntity target, Vec3 look) {
        float damage = HIT_DAMAGE;
        // Water-sensitive creatures without an element (endermen) take double; elemental ones go by the chart.
        if (target.isSensitiveToWater() && CreatureElements.elementOf(target) == null) {
            damage *= 2f;
        }
        damage *= ElementalReactions.waterHit(target, WET_TICKS);
        // The stream decides the knockback, not the damage.
        Vec3 motion = target.getDeltaMovement();
        SpellDamage.hurtMultiHit(target, SpellDamage.source(caster.level(), Element.WATER, caster, caster), damage);
        target.setDeltaMovement(motion.add(look.x * PUSH, 0.02, look.z * PUSH));
        target.hurtMarked = true;
    }

    /** Where the stream lands, the spray soaks and hits whoever's close by (that the caster's magic may touch). */
    private static void splash(ServerPlayer caster, Vec3 at, List<LivingEntity> alreadyHit) {
        ServerLevel level = caster.serverLevel();
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(1.5),
                e -> !alreadyHit.contains(e) && SpellTargets.canAffect(caster, e)
                        && e.getBoundingBox().getCenter().distanceTo(at) <= 1.5 + e.getBbWidth() / 2)) {
            float damage = HIT_DAMAGE * 0.5f * ElementalReactions.waterHit(nearby, WET_TICKS);
            SpellDamage.hurtMultiHit(nearby, SpellDamage.source(level, Element.WATER, caster, caster), damage);
        }
        level.sendParticles(ParticleTypes.SPLASH, at.x, at.y, at.z, 12, 0.6, 0.2, 0.6, 0.15);
    }

    /** The stream puts out fires where it lands. */
    private static void douse(ServerLevel level, BlockHitResult hit) {
        boolean hissed = false;
        BlockPos center = hit.getBlockPos();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            if (level.getBlockState(pos).is(BlockTags.FIRE)) {
                level.removeBlock(pos, false);
                hissed = true;
            }
        }
        if (hissed) {
            Vec3 at = hit.getLocation();
            level.playSound(null, at.x, at.y, at.z, SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.7f, 1.2f);
        }
    }
}
