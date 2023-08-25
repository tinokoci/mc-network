package net.exemine.core.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.InstanceUtil;

import java.util.List;

public class HubCommand extends BaseCommand<CoreUser, CoreData> {

    public HubCommand() {
        super(List.of("hub", "lobby"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (InstanceUtil.isType(InstanceType.HUB) && !user.isEqualOrAbove(RankType.STAFF)) {
            user.sendMessage(CC.RED + "You're already on the hub server.");
            return;
        }
        if (!user.sendToHub()) {
            user.sendMessage(CC.RED + "There are no hub servers available at the moment.");
        }
    }
}