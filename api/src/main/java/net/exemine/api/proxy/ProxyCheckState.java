package net.exemine.api.proxy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ProxyCheckState {

    NORMAL("Normal"),
    WHITELISTED("Whitelisted"),
    BLACKLISTED("Blacklisted");

    private final String name;

    public static ProxyCheckState get(String name) {
        return Arrays.stream(values())
                .filter(state -> state.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
