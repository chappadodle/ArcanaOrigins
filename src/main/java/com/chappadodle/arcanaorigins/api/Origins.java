package com.chappadodle.arcanaorigins.api;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

/** The eight origins and their elements (see the design spec). */
public final class Origins {
    private static final Map<String, Element> ELEMENTS = Map.of(
            "emberborn", Element.FIRE,
            "tideborn", Element.WATER,
            "rimeborn", Element.ICE,
            "galeborn", Element.WIND,
            "stoneborn", Element.EARTH,
            "prismborn", Element.CRYSTAL,
            "stormborn", Element.LIGHTNING,
            "dawnborn", Element.RADIANCE);

    private Origins() {
    }

    /** The element of the origin called {@code name} ("emberborn"), or null for one that isn't ours. */
    @Nullable
    public static Element elementOf(String name) {
        return ELEMENTS.get(name);
    }

    public static Map<String, Element> all() {
        return ELEMENTS;
    }
}
