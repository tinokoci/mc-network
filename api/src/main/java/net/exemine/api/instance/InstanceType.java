package net.exemine.api.instance;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum InstanceType {

    HUB("Hub"),
    UHC("UHC"),
    FFA("FFA"),
    UNKNOWN("Unknown");

    private final String name;

    public static InstanceType get(String name, boolean fallback) {
        return Arrays.stream(values())
                .filter(value -> value.name().equalsIgnoreCase(name) || value.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(fallback ? UNKNOWN : null);
    }
}
