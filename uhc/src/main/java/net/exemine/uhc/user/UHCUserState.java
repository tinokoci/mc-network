package net.exemine.uhc.user;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.exemine.api.util.string.CC;

@NoArgsConstructor
@AllArgsConstructor
public enum UHCUserState {

    LOBBY,
    PRACTICE,
    SCATTER,
    IN_GAME,
    SPECTATOR,
    MODERATOR(CC.AQUA, "[UHC-Mod]", "[M]"),
    SUPERVISOR(CC.PURPLE, "[Supervisor]", "[S]"),
    HOST(CC.RED, "[UHC-Host]", "[H]");

    private String staffColor;
    private String staffLongPrefix;
    private String staffShortPrefix;

    public String getStaffLongPrefix() {
        if (staffLongPrefix == null) return "";
        return staffColor + staffLongPrefix + " ";
    }

    public String getStaffShortPrefix() {
        if (staffShortPrefix == null) return "";
        return staffColor + staffShortPrefix + " ";
    }
}
