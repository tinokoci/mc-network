package net.exemine.core.command.staff;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class SurvivalCommand extends BaseCommand<CoreUser, CoreData> {

    public SurvivalCommand() {
        super(List.of("survival", "gms"), Rank.ADMIN);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
       if (args.length != 0 && args.length != 1) {
           user.sendMessage(CC.RED + "Usage /survival [user]");
           return;
       }
       user.performCommand("gamemode survival " + StringUtil.join(args));
    }
}
