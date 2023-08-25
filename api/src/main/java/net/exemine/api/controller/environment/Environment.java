package net.exemine.api.controller.environment;

import java.util.Arrays;

public enum Environment {

    PRODUCTION,
    TEST;

    public static Environment get(String name) {
        return Arrays.stream(values())
                .filter(value -> value.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(TEST);
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
