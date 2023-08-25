package net.exemine.api.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnumUtilTest {

    @Test
    public void testGetNext() {
        assertEquals(Color.GREEN, EnumUtil.getNext(Color.RED));
        assertEquals(Color.DARK_BLUE, EnumUtil.getNext(Color.GREEN));
        assertEquals(Color.RED, EnumUtil.getNext(Color.DARK_BLUE));
    }

    @Test
    public void testGetPrevious() {
        assertEquals(Color.DARK_BLUE, EnumUtil.getPrevious(Color.RED));
        assertEquals(Color.RED, EnumUtil.getPrevious(Color.GREEN));
        assertEquals(Color.GREEN, EnumUtil.getPrevious(Color.DARK_BLUE));
    }

    @Test
    public void testGetAdjacent() {
        assertEquals(Color.GREEN, EnumUtil.getAdjacent(Color.RED, true));
        assertEquals(Color.DARK_BLUE, EnumUtil.getAdjacent(Color.GREEN, true));
        assertEquals(Color.RED, EnumUtil.getAdjacent(Color.DARK_BLUE, true));

        assertEquals(Color.DARK_BLUE, EnumUtil.getAdjacent(Color.RED, false));
        assertEquals(Color.RED, EnumUtil.getAdjacent(Color.GREEN, false));
        assertEquals(Color.GREEN, EnumUtil.getAdjacent(Color.DARK_BLUE, false));
    }

    @Test
    public void testGetName() {
        assertEquals("Red", EnumUtil.getName(Color.RED));
        assertEquals("Green", EnumUtil.getName(Color.GREEN));
        assertEquals("Dark Blue", EnumUtil.getName(Color.DARK_BLUE));
    }

    private enum Color {
        RED,
        GREEN,
        DARK_BLUE
    }
}