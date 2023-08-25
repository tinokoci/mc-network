package net.exemine.api.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EnumUtil {

    public static <T extends Enum<T>> T getNext(T type) {
        T[] values = type.getDeclaringClass().getEnumConstants();
        T fallback = values[0];
        if (type.ordinal() + 1 == values.length) return fallback;
        return Arrays.stream(values)
                .filter(c -> c.ordinal() - 1 == type.ordinal())
                .findFirst()
                .orElse(fallback);
    }

    public static <T extends Enum<T>> T getPrevious(T type) {
        T[] values = type.getDeclaringClass().getEnumConstants();
        T fallback = values[values.length - 1];
        if (type.ordinal() == 0) return fallback;
        return Arrays.stream(values)
                .filter(c -> c.ordinal() + 1 == type.ordinal())
                .findFirst()
                .orElse(fallback);
    }

    public static <T extends Enum<T>> T getAdjacent(T type, boolean next) {
        return next ? getNext(type) : getPrevious(type);
    }

    public static <T extends Enum<T>> String getName(T type) {
        return Arrays.stream(type.name().split("_"))
                .map(word -> StringUtil.capitalize(word.toLowerCase()))
                .collect(Collectors.joining(" "));
    }
}
