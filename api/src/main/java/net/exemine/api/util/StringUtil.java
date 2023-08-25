package net.exemine.api.util;

import java.util.ArrayList;
import net.exemine.api.controller.ApiController;
import net.exemine.api.controller.platform.exception.IllegalPlatformException;
import net.exemine.api.util.apache.RandomStringUtils;
import net.exemine.api.util.spigot.ChatColor;
import net.exemine.api.util.string.CC;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StringUtil {

    public static String join(String[] array, int startingIndex, String separator) {
        StringBuilder builder = new StringBuilder();

        IntStream.range(startingIndex, array.length).forEach(i -> {
            builder.append(array[i]);
            if (i != array.length - 1) {
                builder.append(separator);
            }
        });
        return builder.toString();
    }

    public static String join(String[] array, int startingIndex) {
        return join(array, startingIndex, " ");
    }

    public static String join(String[] array) {
        return join(array, 0);
    }

    public static String listToString(List<String> list, String separatorColor) {
        return list.stream().collect(Collectors.joining((separatorColor == null ? "" : separatorColor) + ", "));
    }

    public static String listToString(List<String> list) {
        return listToString(list, null);
    }

    public static <E extends Enum<E>> String enumToString(Enum<E>[] enumArray) {
        return enumToString(List.of(enumArray));
    }

    public static <E extends Enum<E>> String enumToString(List<Enum<E>> enumList) {
        return listToString(enumList.stream().map(Enum::name).collect(Collectors.toList()), null);
    }

    public static List<String> stringToList(String text) {
        return Arrays.stream(text.split(", ")).collect(Collectors.toList());
    }

    public static String limitLength(String word, int limit) {
        return word.length() > limit ? word.substring(0, limit) : word;
    }

    public static String capitalize(String text) {
        if (text == null || text.length() == 0) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static String formatBooleanStatus(boolean value) {
        if (!ApiController.getInstance().isMinecraftPlatform()) {
            throw new IllegalPlatformException();
        }
        return value ? CC.GREEN + "Yes" : CC.RED + "No";
    }

    public static String formatBooleanCommand(boolean value) {
        if (!ApiController.getInstance().isMinecraftPlatform()) {
            throw new IllegalPlatformException();
        }
        return value ? CC.GREEN + "now" : CC.RED + "no longer";
    }

    public static String formatBooleanLong(boolean value, boolean includeColors) {
        if (ApiController.getInstance().isMinecraftPlatform() && includeColors) {
            return value ? CC.GREEN + "Enabled" : CC.RED + "Disabled";
        }
        return value ? "Enabled" : "Disabled";
    }

    public static String formatBooleanLong(boolean value) {
        return formatBooleanLong(value, true);
    }

    public static String formatNumber(int number) {
        return String.format("%,d", number);
    }

    public static String formatRatio(int a, int b) {
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        format.setRoundingMode(RoundingMode.HALF_UP);

        float ratio = (float) a / Math.max(1, b);
        return format.format(ratio);
    }

    public static String formatDecimalNumber(double input, int decimals) {
        String format = "%." + decimals + "f";
        return String.format(format, input);
    }

    public static String formatDecimalNumber(float input, int decimals) {
        return formatDecimalNumber((double) input, decimals);
    }

    public static String getOrdinal(int number) {
        if (number >= 11 && number <= 13) {
            return number + "th";
        }
        switch (number % 10) {
            case 1:
                return number + "st";
            case 2:
                return number + "nd";
            case 3:
                return number + "rd";
            default:
                return number + "th";
        }
    }

    public static String formatEnumName(String name) {
        return Arrays.stream(name.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    public static String[] formatLore(String text, ChatColor color) {
        int max = 32;
        String[] words = text.split(" ");

        List<String> result = new ArrayList<>();
        StringBuilder builder = new StringBuilder(String.valueOf(color));

        for (String word : words) {
            if (builder.toString().length() > max) {
                result.add(builder.toString());
                builder = new StringBuilder(String.valueOf(color));
            }
            builder.append(word).append(" ");
        }
        result.add(builder.toString());
        return result.toArray(new String[0]);
    }

    public static String getPlural(Number count) {
        return count.intValue() == 1 ? "" : "s";
    }

    public static String randomID() {
        return RandomStringUtils.randomAlphanumeric(16);
    }

    public static String randomID(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    public static boolean isLong(String... input) {
        for (String element : input) {
            try {
                Long.parseLong(element);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDouble(String... input) {
        for (String element : input) {
            try {
                Double.parseDouble(element);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean isInteger(String... input) {
        for (String element : input) {
            try {
                Integer.parseInt(element);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
}
