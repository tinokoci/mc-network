package net.exemine.api.util;

import net.exemine.api.controller.ApiController;
import net.exemine.api.controller.platform.Platform;
import net.exemine.api.util.string.CC;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilTest {



    @Test
    void join1() {
        String[] givenValue = {"one", "two", "three", "four", "five"};
        String expectedValue = "two three four five";
        assertEquals(expectedValue, StringUtil.join(givenValue, 1, " "));
    }

    @Test
    void join2() {
        String[] givenValue = {"one", "two", "three", "four", "five"};
        String expectedValue = "two three four five";
        assertEquals(expectedValue, StringUtil.join(givenValue, 1));
    }

    @Test
    void join3() {
        String[] givenValue = {"one", "two", "three", "four", "five"};
        String expectedValue = "one two three four five";
        assertEquals(expectedValue, StringUtil.join(givenValue));
    }

    @Test
    void listToString1() {
        List<String> givenValue = List.of("one", "two", "three");
        String separatorColor = "red";
        String expectedValue = "one" + separatorColor + ", two" + separatorColor + ", three";
        assertEquals(expectedValue, StringUtil.listToString(givenValue, separatorColor));
    }

    @Test
    void listToString2() {
        List<String> givenValue = List.of("one", "two", "three");
        String expectedValue = "one, two, three";
        assertEquals(expectedValue, StringUtil.listToString(givenValue));
    }

    @Test
    void enumToString() {
        String expectedValue = "ONE, TWO, THREE";
        assertEquals(expectedValue, StringUtil.enumToString(TestEnum1.values()));
    }

    @Test
    void stringToList() {
        String givenValue = "one, two, three";
        List<String> expectedValue = List.of("one", "two", "three");
        assertEquals(expectedValue, StringUtil.stringToList(givenValue));
    }

    @Test
    void limitLength() {
        String givenValue = "abcdefghijk";
        String expectedValue = "abcdefghij";
        assertEquals(expectedValue, StringUtil.limitLength(givenValue, 10));
    }

    @Test
    void formatTrueBoolean() {
        ApiController.getInstance().setPlatform(Platform.MINECRAFT);
        boolean givenValue = true;
        String expectedValue = CC.GREEN + "Yes";
        assertEquals(expectedValue, StringUtil.formatBooleanStatus(givenValue));
    }

    @Test
    void formatFalseBoolean() {
        ApiController.getInstance().setPlatform(Platform.MINECRAFT);
        boolean givenValue = false;
        String expectedValue = CC.RED + "No";
        assertEquals(expectedValue, StringUtil.formatBooleanStatus(givenValue));
    }

    @Test
    void randomID() {
        boolean fail = false;
        List<String> ids = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            String id = StringUtil.randomID();
            if (ids.contains(id)) {
                fail = true;
                break;
            }
            ids.add(id);
        }
        assertFalse(fail);
    }

    @Test
    void isLong() {
        String longValueAsString = "1000000000000000000";
        String floatValueAsString = "10.5";

        assertFalse(StringUtil.isLong(longValueAsString, longValueAsString, floatValueAsString));
        assertTrue(StringUtil.isLong(longValueAsString, longValueAsString));
    }

    @Test
    void isInteger() {
        String intValueAsString = "10000000";
        String floatValueAsString = "10.5";

        assertFalse(StringUtil.isInteger(intValueAsString, intValueAsString, floatValueAsString));
        assertTrue(StringUtil.isInteger(intValueAsString, intValueAsString));
    }

    @Test
    public void formatNumber() {
        assertEquals("1,234", StringUtil.formatNumber(1234));
        assertEquals("12,345", StringUtil.formatNumber(12345));
        assertEquals("123,456", StringUtil.formatNumber(123456));
        assertEquals("1,234,567", StringUtil.formatNumber(1234567));
    }

    @Test
    void formatDecimalNumber() {
        double givenValueAsDouble = 0.23723D;
        float givenValueAsFloat = 0.23723f;
        String expectedValue = "0.24";

        assertEquals(expectedValue, StringUtil.formatDecimalNumber(givenValueAsFloat, 2));
        assertEquals(expectedValue, StringUtil.formatDecimalNumber(givenValueAsDouble, 2));
    }

    @Test
    void formatEnumName() {
        assertEquals("One", StringUtil.formatEnumName(TestEnum2.ONE.name()));
        assertEquals("Twenty Four", StringUtil.formatEnumName(TestEnum2.TWENTY_FOUR.name()));
        assertEquals("Test Test Test", StringUtil.formatEnumName(TestEnum2.TEST_TEST_TEST.name()));
    }

    private enum TestEnum1 {
        ONE,
        TWO,
        THREE,
    }

    private enum TestEnum2 {
        ONE,
        TWENTY_FOUR,
        TEST_TEST_TEST
    }
 }