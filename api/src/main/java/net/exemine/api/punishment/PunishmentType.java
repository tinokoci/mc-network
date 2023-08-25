package net.exemine.api.punishment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.exemine.api.util.string.CC;

@Getter
@AllArgsConstructor
public enum PunishmentType {

    KICK("Kick", "kicked", null, "kicks", CC.BOLD_YELLOW, "IRON_BLOCK"),
    MUTE("Mute", "muted", "unmuted", "mutes", CC.BOLD_GOLD, "GOLD_BLOCK"),
    BAN("Ban", "banned", "unbanned", "bans", CC.BOLD_RED, "REDSTONE_BLOCK"),
    IP_BAN("IP Ban", "IP banned", "unbanned", "bans", CC.BOLD_RED, "COAL_BLOCK"),
    BLACKLIST("Blacklist", "blacklisted", "unblacklisted", "blacklists", CC.BOLD_DARK_RED, "BEDROCK");

    private final String name;
    private final String format;
    private final String pardon;
    private final String plural;
    private final String menuColor;
    private final String material;

    public boolean isOrGreaterThan(PunishmentType type) {
        return ordinal() >= type.ordinal();
    }
}
