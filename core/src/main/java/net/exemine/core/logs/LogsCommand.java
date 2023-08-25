package net.exemine.core.logs;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.logs.procedure.LogsProcedure;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class LogsCommand extends BaseCommand<CoreUser, CoreData> {

    public LogsCommand() {
        super(List.of("logs", "log"), Rank.SENIOR_MOD);
        setAsync(true);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (args.length != 1) {
            user.sendMessage(CC.RED + "Usage: /logs <player>");
            return;
        }
        userService.fetch(args[0]).ifPresentOrElse(
                target -> {
                    if (target.isEqualOrAbove(user)) {
                        user.sendMessage(CC.RED + "You cannot see " + target.getColoredRealName() + CC.RED + "'s logs.");
                        return;
                    }
                    new LogsProcedure(user);
                    new LogsMenu(user, target).open();
                },
                () -> user.sendMessage(Lang.USER_NEVER_PLAYED)
        );
    }
}
