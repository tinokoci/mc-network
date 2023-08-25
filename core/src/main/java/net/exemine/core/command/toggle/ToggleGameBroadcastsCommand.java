package net.exemine.core.command.toggle;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class ToggleGameBroadcastsCommand extends BaseCommand<CoreUser, CoreData> {

    public ToggleGameBroadcastsCommand() {
        super(List.of("togglegamebroadcasts", "gamebroadcasts"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        data.setGameBroadcasts(!data.isGameBroadcasts());
        user.saveData(true);
        user.sendMessage(CC.PURPLE + "[Settings] " + CC.GRAY + "You can " + StringUtil.formatBooleanCommand(data.isGameBroadcasts()) + CC.GRAY + " see alerts about ongoing or soon to start games.");
    }
}


