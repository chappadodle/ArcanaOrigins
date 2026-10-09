package com.chappadodle.arcanaorigins.api;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OriginsTest {
    @Test
    void eightOriginsOneForEachElement() {
        assertEquals(8, Origins.all().size());
        assertEquals(EnumSet.allOf(Element.class), EnumSet.copyOf(Origins.all().values()));
    }

    @Test
    void namesFindTheirElement() {
        assertEquals(Element.FIRE, Origins.elementOf("emberborn"));
        assertEquals(Element.ICE, Origins.elementOf("rimeborn"));
        assertNull(Origins.elementOf("frostborn"));
        assertNull(Origins.elementOf("human"));
    }
}
