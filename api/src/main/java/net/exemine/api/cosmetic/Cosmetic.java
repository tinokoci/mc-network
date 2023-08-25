package net.exemine.api.cosmetic;

public interface Cosmetic {

    CosmeticType getType();

    String name();

    default String getPermission() {
        return "cosmetic." + getType().name().toLowerCase() + '.' + name().toLowerCase();
    }
}
