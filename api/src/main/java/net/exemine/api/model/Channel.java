package net.exemine.api.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.controller.ApiController;
import net.exemine.api.instance.Instance;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;

@RequiredArgsConstructor
@Getter
public enum Channel {

    ADMIN("[Admin]", CC.GOLD, CC.YELLOW, CC.WHITE, Rank.DEVELOPER),
    STAFF("[Staff]", CC.BLUE, CC.AQUA, CC.AQUA, Rank.TRIAL_MOD),
    DEFAULT(null, null, null, null, null);

    private final String prefix;
    private final String formatColor;
    private final String infoColor;
    private final String valueColor;
    private final Rank rank;

    public String getChatFormat(Instance instance, String name, String message) {
        return format(instance.getName(), name, CC.GRAY + ": " + infoColor + message);
    }

    public String format(String instanceName, String name, String append) {
        ApiController.requireMinecraftPlatform();
        String instanceInfo = instanceName == null
                ? ""
                : CC.GRAY + '[' + instanceName + "] ";
        return formatColor + prefix + ' ' + instanceInfo + CC.RESET + name + append;
    }

    public String format(String name, String append) {
        return format(null, name, append);
    }
}