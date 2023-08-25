package net.exemine.core.cosmetic.color;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.exemine.api.cosmetic.Cosmetic;
import net.exemine.api.cosmetic.CosmeticType;
import net.exemine.api.util.spigot.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum ColorType implements Cosmetic {

    DARK_GREEN("Dark Green", ChatColor.DARK_GREEN),
    DARK_AQUA("Dark Aqua", ChatColor.DARK_AQUA),
    DARK_RED("Dark Red", ChatColor.DARK_RED),
    PURPLE("Purple", ChatColor.DARK_PURPLE),
    GOLD("Gold", ChatColor.GOLD),
    GRAY("Gray", ChatColor.GRAY),
    DARK_GRAY("Dark Gray", ChatColor.DARK_GRAY),
    BLUE("Blue", ChatColor.BLUE),
    GREEN("Green", ChatColor.GREEN),
    AQUA("Aqua", ChatColor.AQUA),
    RED("Red", ChatColor.RED),
    PINK("Pink", ChatColor.LIGHT_PURPLE),
    YELLOW("Yellow", ChatColor.YELLOW),
    WHITE("White", ChatColor.WHITE);

    private final String name;
    private final ChatColor format;

    @Override
    public CosmeticType getType() {
        return CosmeticType.COLOR;
    }

    @Nullable
    public static ColorType get(String name) {
        return Arrays.stream(ColorType.values())
                .filter(prefix -> prefix.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}