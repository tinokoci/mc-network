package net.exemine.core.rank.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.rank.menu.RankHistoryMenu;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class RankHistoryCommand extends BaseCommand<CoreUser, CoreData> {

    public RankHistoryCommand() {
        super(List.of("rankhistory", "ranks", "grants"), Rank.MANAGER);
        setAsync(true);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (args.length != 1) {
            user.sendMessage(CC.RED + "Usage: /rankhistory <player>");
            return;
        }
        userService.fetch(args[0]).ifPresentOrElse(
                target -> new RankHistoryMenu(user, target).open(),
                () -> user.sendMessage(Lang.USER_NEVER_PLAYED)
        );
    }
}
