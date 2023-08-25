package net.exemine.hub.command;

import net.exemine.api.data.impl.HubData;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.hub.user.HubUser;
import net.exemine.hub.user.event.PlayerToggleHubVisibilityEvent;
import org.bukkit.Bukkit;

import java.util.List;

public class VisibilityCommand extends BaseCommand<HubUser, HubData> {

    public VisibilityCommand() {
        super(List.of("togglevisibility", "visibility"));
    }

    @Override
    public void execute(HubUser user, HubData data, String[] args) {
        data.setPlayerVisibility(!data.isPlayerVisibility());
        user.saveData(true);
        user.sendMessage(CC.PURPLE + "[Hub] " + CC.GRAY + "You can " + StringUtil.formatBooleanCommand(data.isPlayerVisibility()) + CC.GRAY + " see regular players.");

        PlayerToggleHubVisibilityEvent event = new PlayerToggleHubVisibilityEvent(user, data.isPlayerVisibility());
        Bukkit.getPluginManager().callEvent(event);
    }
}
