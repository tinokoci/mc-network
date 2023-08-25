package net.exemine.api.util;

import net.exemine.api.util.spigot.ChatColor;
import net.exemine.api.util.string.CC;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CCTest {

    @Test
    void translate() {
        String givenValue = "&c&lTest";
        String expectedValue = givenValue.replace('&', ChatColor.COLOR_CHAR);
        assertEquals(expectedValue, CC.translate(givenValue));
    }
}