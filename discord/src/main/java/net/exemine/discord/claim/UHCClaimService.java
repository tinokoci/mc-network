package net.exemine.discord.claim;

import lombok.Getter;
import net.exemine.api.util.StringUtil;
import net.exemine.discord.Discord;

@Getter
public class UHCClaimService {

    private final String modalId = "claim";
    private final String buttonAcceptId = "continue";
    private final String buttonRejectId = "close";

    public UHCClaimService(Discord discord) {
        discord.getJda().addEventListener(new UHCClaimListener(discord.getBulkDataService(), discord.getDataService(), discord.getMatchService(), this));
    }

    public boolean isMode(String input) {
        if (input.equalsIgnoreCase("ffa")) return true;
        int length = input.length();
        return (length == 3 || length == 4)
                && input.toLowerCase().startsWith("to")
                && StringUtil.isInteger(input.substring(2, length));
    }
}
