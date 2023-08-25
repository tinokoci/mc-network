package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.util.StringUtil;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.user.UHCUser;

import java.util.List;

public class TeamListCommand extends BaseCommand<UHCUser, UHCData> {

    public TeamListCommand() {
        super(List.of("teamlist", "tl"));
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        user.performCommand("team list " + StringUtil.join(args));
    }
}
