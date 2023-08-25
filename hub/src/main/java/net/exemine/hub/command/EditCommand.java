package net.exemine.hub.command;

import net.exemine.api.data.impl.HubData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.hub.user.HubUser;

import java.util.List;

public class EditCommand extends BaseCommand<HubUser, HubData> {

    public EditCommand() {
        super(List.of("toggledit", "edit", "build", "place", "break"), Rank.DEVELOPER);
    }

    @Override
    public void execute(HubUser user, HubData data, String[] args) {
        user.setBuild(!data.isEdit());
        user.sendMessage(CC.PURPLE + "[Dev] " + CC.GRAY + "You can " + StringUtil.formatBooleanCommand(data.isEdit()) + CC.GRAY + " edit the map in the hub.");
        user.saveData(true);
    }
}
