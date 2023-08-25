package net.exemine.api.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavaUtilTest {

    @Test
    void testIsAnsiSupported() {
        boolean ansiSupported = JavaUtil.isAnsiSupported();

        String os = System.getProperty("os.name").toLowerCase();
        String term = System.getenv("TERM");

        // Only run the test on Unix-based systems (Linux, macOS) with TERM variable set
        if (os.contains("win") || term == null) return;

        if (!term.contains("dumb")) {
            assertTrue(ansiSupported, "ANSI should be supported on this system.");
        } else {
            System.out.println("Skipping test on this system. ANSI support cannot be determined.");
        }
    }
}
