package net.exemine.core.punishment.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.punishment.menu.PunishmentListMenu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;

import java.util.List;

public class HistoryCommand extends BaseCommand<CoreUser, CoreData> {

    private final UserService<CoreUser, CoreData> userService;

    public HistoryCommand(UserService<CoreUser, CoreData> userService) {
        super(List.of("history", "punishmenthistory"), Rank.TRIAL_MOD);
        this.userService = userService;
        setAsync(true);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (args.length != 1) {
            user.sendMessage(CC.RED + "Usage: /history <user>");
            return;
        }
        userService.fetch(args[0]).ifPresentOrElse(target -> {
            if (target.getBulkData().getPunishmentList().isEmpty()) {
                user.sendMessage(target.getColoredRealName() + CC.RED + " doesn't have any punishments.");
                return;
            }
            new PunishmentListMenu(user, target).open();
        }, () -> user.sendMessage(Lang.USER_NEVER_PLAYED));
    }
}
