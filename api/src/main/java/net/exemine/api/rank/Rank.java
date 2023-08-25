package net.exemine.api.rank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.util.spigot.ChatColor;
import net.exemine.api.util.string.CC;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum Rank {

    // Team
    OWNER("Owner", RankType.STAFF, ChatColor.DARK_RED),
    ADMIN("Admin", "Administrator", RankType.STAFF, ChatColor.RED),
    DEVELOPER("Developer", RankType.STAFF, ChatColor.RED),
    MANAGER("Manager", RankType.STAFF, ChatColor.LIGHT_PURPLE),
    SENIOR_MOD("Senior Mod", "Senior Moderator", RankType.STAFF, ChatColor.DARK_PURPLE),
    MOD_PLUS("Mod+", "Moderator+", RankType.STAFF, ChatColor.DARK_AQUA),
    MOD("Mod", "Moderator", RankType.STAFF, ChatColor.DARK_AQUA),
    TRIAL_MOD("Trial Mod", "Trial Moderator", RankType.STAFF, ChatColor.DARK_GREEN),
    RETIRED("Retired", RankType.DONATOR, ChatColor.GOLD),
    COUNCIL("Council", RankType.DONATOR, ChatColor.AQUA),

    // Content Creators
    PARTNER("Partner", RankType.CONTENT_CREATOR, ChatColor.GREEN),
    FAMOUS("Famous", RankType.CONTENT_CREATOR, ChatColor.YELLOW),
    YOUTUBE("YouTube", RankType.CONTENT_CREATOR, ChatColor.LIGHT_PURPLE),
    TWITCH("Twitch", RankType.CONTENT_CREATOR, ChatColor.LIGHT_PURPLE),

    // Donators
    MASTER("Master", RankType.DONATOR, ChatColor.GOLD),
    ELITE("Elite", RankType.DONATOR, ChatColor.DARK_PURPLE),
    PRIME("Prime", RankType.DONATOR, ChatColor.BLUE),
    ALPHA("Alpha", RankType.DONATOR, ChatColor.AQUA),

    // Regular
    DEFAULT("Default", RankType.DEFAULT, ChatColor.GREEN);

    private final String name;
    private String discordRole;
    private final RankType type;
    private final ChatColor color;

    public String getDiscordRole() {
        return Optional.ofNullable(discordRole).orElse(name);
    }

    public String getPrefix() {
        if (this == DEFAULT) {
            return getColor();
        }
        return CC.GRAY + '[' + getDisplayName() + CC.GRAY + ']' + ' ' + getColor();
    }

    public String getColor() {
        return color.toString();
    }

    public String getDisplayName() {
        return getColor() + name;
    }

    public boolean isEqual(RankType rankType) {
        return this.type == rankType;
    }

    public boolean isEqualOrAbove(RankType rankType) {
        return type.ordinal() <= rankType.ordinal();
    }

    public boolean isEqualOrAbove(Rank rank) {
        return ordinal() <= rank.ordinal();
    }

    public boolean isEqual(Rank rank) {
        return this == rank;
    }

    public boolean isAbove(Rank rank) {
        return ordinal() < rank.ordinal();
    }

    public int getPriority() {
        return ordinal();
    }

    public static Rank get(String name) {
        return Arrays.stream(values())
                .filter(rank -> rank.name().equalsIgnoreCase(name)
                        || rank.getName().equalsIgnoreCase(name)
                        || rank.getDiscordRole().equalsIgnoreCase(name)
                )
                .findFirst()
                .orElse(null);
    }
}
