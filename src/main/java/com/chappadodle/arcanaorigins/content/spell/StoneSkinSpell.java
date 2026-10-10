package com.chappadodle.arcanaorigins.content.spell;

import com.chappadodle.arcanaorigins.ArcanaOrigins;
import com.chappadodle.arcanaorigins.api.Element;
import com.chappadodle.arcanaorigins.api.ShieldSpell;
import com.chappadodle.arcanaorigins.api.SpellDamage;
import com.chappadodle.arcanaorigins.api.SpellShield;
import com.chappadodle.arcanaorigins.magic.Spell;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

/**
 * Stoneborn's Stone Skin (Elemental Arcana's, at one fixed strength): stones orbit you for 10
 * seconds, soaking up 8 damage, adding armour and making you immovable. Melee attackers are hurt
 * back (Thorns); raised just before a hit, it blocks it outright and throws the attacker back
 * (Brace). When it breaks, the stones fly outward and shove everything close away.
 */
public class StoneSkinSpell implements Spell, ShieldSpell {
    public static final String NAME = "stone_skin";
    private static final int LEVEL = 4;
    private static final float STRENGTH = 8f;
    private static final int DURATION = 200;
    private static final double ARMOR = 6.0;

    private static final ResourceLocation SHARD_MODEL = ArcanaOrigins.id("spell/stone_shard");
    private static final ResourceLocation SKIN_ARMOR = ArcanaOrigins.id("stone_skin_armor");
    private static final ResourceLocation SKIN_KNOCKBACK = ArcanaOrigins.id("stone_skin_knockback");
    private static final int PARRY_WINDOW_TICKS = 6;
    private static final double BURST_RADIUS = 3.0;

    @Override
    public boolean cast(ServerPlayer caster) {
        SpellShield.raise(caster, NAME, STRENGTH, DURATION, LEVEL, Map.of());
        return true;
    }

    // ---- shield lifecycle ----

    @Override
    public void onShieldRaised(ServerPlayer player, SpellShield shield) {
        addModifier(player.getAttribute(Attributes.ARMOR), SKIN_ARMOR, ARMOR);
        addModifier(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), SKIN_KNOCKBACK, 1.0);
        ServerLevel level = player.serverLevel();
        for (int i = 0; i < 24; i++) {
            float angle = i * Mth.TWO_PI / 24;
            level.sendParticles(rubble(), player.getX() + Mth.cos(angle) * 1.1, player.getY(0.4),
                    player.getZ() + Mth.sin(angle) * 1.1, 2, 0.1, 0.3, 0.1, 0.02);
        }
        playAt(player, SoundEvents.STONE_PLACE, 1.2f, 0.7f);
        playAt(player, SoundEvents.GRAVEL_PLACE, 1f, 0.8f);
    }

    private static void addModifier(AttributeInstance attribute, ResourceLocation id, double amount) {
        if (attribute != null && !attribute.hasModifier(id)) {
            attribute.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void removeModifier(AttributeInstance attribute, ResourceLocation id) {
        if (attribute != null) {
            attribute.removeModifier(id);
        }
    }

    @Override
    public void onShieldEnd(ServerPlayer player, SpellShield shield, boolean broken) {
        removeModifier(player.getAttribute(Attributes.ARMOR), SKIN_ARMOR);
        removeModifier(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), SKIN_KNOCKBACK);
        ServerLevel level = player.serverLevel();
        if (!broken) {
            level.sendParticles(rubble(), player.getX(), player.getY(0.5), player.getZ(), 14, 0.6, 0.5, 0.6, 0.03);
            playAt(player, SoundEvents.GRAVEL_BREAK, 0.8f, 0.9f);
            return;
        }
        // Burst: the fragments shove everything close back.
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(BURST_RADIUS),
                e -> e != player && e.isAlive() && e.distanceTo(player) <= BURST_RADIUS
                        && com.chappadodle.arcanaorigins.api.SpellTargets.canAffect(player, e))) {
            Vec3 away = nearby.position().subtract(player.position()).normalize();
            nearby.knockback(1.0, -away.x, -away.z);
            nearby.hurtMarked = true;
        }
        level.sendParticles(rubble(), player.getX(), player.getY(0.6), player.getZ(), 30, 0.4, 0.4, 0.4, 0.25);
        level.sendParticles(ParticleTypes.POOF, player.getX(), player.getY(0.4), player.getZ(), 6, 0.6, 0.3, 0.6, 0.02);
        playAt(player, SoundEvents.STONE_BREAK, 1.4f, 0.7f);
    }

    // ---- hits ----

    @Override
    public boolean blocksHit(ServerPlayer player, SpellShield shield, DamageSource source, float amount) {
        if (player.level().getGameTime() - shield.raisedAt() <= PARRY_WINDOW_TICKS) {
            brace(player, source, amount);
            return true;
        }
        return false;
    }

    /** Brace: no damage and no knockback; the attacker takes the hit instead and is thrown back. */
    private static void brace(ServerPlayer player, DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity attacker && attacker != player) {
            Vec3 away = attacker.position().subtract(player.position()).normalize();
            attacker.knockback(1.2, -away.x, -away.z);
            attacker.hurtMarked = true;
            SpellDamage.hurtMultiHit(attacker, SpellDamage.source(player.level(), Element.EARTH, player, player), amount);
        }
        ServerLevel level = player.serverLevel();
        level.sendParticles(rubble(), player.getX(), player.getY(0.6), player.getZ(), 26, 0.5, 0.5, 0.5, 0.2);
        level.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY(0.6), player.getZ(), 1, 0, 0, 0, 0);
        playAt(player, SoundEvents.ANVIL_LAND, 1.0f, 1.2f);
        player.displayClientMessage(Component.translatable("message.arcanaorigins.stone_skin.brace").withStyle(ChatFormatting.GOLD), true);
    }

    @Override
    public void onShieldHit(ServerPlayer player, SpellShield shield, DamageSource source, float absorbed) {
        ServerLevel level = player.serverLevel();
        level.sendParticles(rubble(), player.getX(), player.getY(0.6), player.getZ(), 8, 0.5, 0.4, 0.5, 0.08);
        playAt(player, SoundEvents.STONE_HIT, 1f, 0.8f);
        // Thorns, for melee attackers only (the attacker is also what directly hit you).
        if (source.getEntity() instanceof LivingEntity attacker && source.getDirectEntity() == attacker && attacker != player) {
            SpellDamage.hurtMultiHit(attacker, SpellDamage.source(player.level(), Element.EARTH, player, player), 2f + absorbed * 0.3f);
        }
    }

    // ---- visuals ----

    @Override
    public ResourceLocation shardModel() {
        return SHARD_MODEL;
    }

    private static ParticleOptions rubble() {
        return new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState());
    }

    /** A few crumbs drifting around you; also what you see of your own shield in first person. */
    @Override
    public void shieldParticles(Player player, SpellShield shield) {
        if (player.tickCount % 4 != 0) {
            return;
        }
        float angle = player.tickCount * 0.2f;
        double radius = 0.9;
        player.level().addParticle(rubble(),
                player.getX() + Mth.cos(angle) * radius, player.getY(0.4) + player.getRandom().nextDouble() * 0.6,
                player.getZ() + Mth.sin(angle) * radius, 0, 0.01, 0);
    }

    private static void playAt(Entity at, SoundEvent sound, float volume, float pitch) {
        at.level().playSound(null, at.getX(), at.getY(), at.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }
}
