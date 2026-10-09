package com.chappadodle.arcanaorigins.api;

import com.chappadodle.arcanaorigins.ArcanaOrigins;
import com.cyberday1.neoorigins.attachment.OriginAttachments;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Which element a creature has. Creatures born with one are listed in the entity-type tags
 * {@code arcanaorigins:innate/<element>} (blazes are Fire, strays Ice...); a player has the element
 * of the Arcana origin they chose (an Emberborn is Fire), and none otherwise. The game never shows a
 * creature's element; it only changes how spells hit it (Element#multiplierAgainst).
 */
public final class CreatureElements {
    private static final Map<Element, TagKey<EntityType<?>>> INNATE = new EnumMap<>(Element.class);
    private static final ResourceLocation ORIGIN_LAYER = ResourceLocation.fromNamespaceAndPath("neoorigins", "origin");

    static {
        for (Element element : Element.values()) {
            INNATE.put(element, TagKey.create(Registries.ENTITY_TYPE, ArcanaOrigins.id("innate/" + element.name().toLowerCase(Locale.ROOT))));
        }
    }

    private CreatureElements() {
    }

    public static TagKey<EntityType<?>> innateTag(Element element) {
        return INNATE.get(element);
    }

    /** The creature's element, or null if it has none (a player: their Arcana origin's). */
    @Nullable
    public static Element elementOf(LivingEntity entity) {
        if (entity instanceof Player player) {
            return originElement(player);
        }
        return innateElementOf(entity);
    }

    /** The element the creature's type is born with (the innate tags), or null. */
    @Nullable
    public static Element innateElementOf(Entity entity) {
        for (Element element : Element.values()) {
            if (entity.getType().is(INNATE.get(element))) {
                return element;
            }
        }
        return null;
    }

    /** The element of a player's origin, if it's one of ours, else null. */
    @Nullable
    public static Element originElement(Player player) {
        ResourceLocation origin = player.getData(OriginAttachments.originData()).getOrigin(ORIGIN_LAYER);
        return origin != null && origin.getNamespace().equals(ArcanaOrigins.MODID) ? Origins.elementOf(origin.getPath()) : null;
    }
}
