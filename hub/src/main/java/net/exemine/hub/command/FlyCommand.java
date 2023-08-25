package net.exemine.hub.command;

import net.exemine.api.data.impl.HubData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.hub.user.HubUser;

import java.util.List;

public class FlyCommand extends BaseCommand<HubUser, HubData> {

    public FlyCommand() {
        super(List.of("togglefly", "fly", "flight"), Rank.ALPHA);
    }

    @Override
    public void execute(HubUser user, HubData data, String[] args) {
        user.setFlight(!data.isFlight());
        user.saveData(true);
        user.sendMessage(CC.PURPLE + "[Perk] " + CC.GRAY + "You can " + StringUtil.formatBooleanCommand(data.isFlight()) + CC.GRAY + " fly in the hub.");
    }
}
