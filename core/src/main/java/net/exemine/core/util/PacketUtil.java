package net.exemine.core.util;

import java.util.stream.IntStream;

public class PacketUtil {

    public static String MAX_TEAM_NAME_VALUE = "999999";

    public static String convertNumberToTeamName(int number) {
        String textNumber = String.valueOf(number);
        if (number < 10) return textNumber;

        int nines = textNumber.length() - 1;

        StringBuilder builder = new StringBuilder();
        IntStream.range(0, nines).forEach(nine -> builder.append('9'));

        char[] chars = textNumber.toCharArray();
        builder.append(chars[chars.length - 1]);

        return builder.toString();
    }
}