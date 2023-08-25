package net.exemine.core.punishment.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.punishment.menu.selector.MuteMenu;
import net.exemine.core.punishment.menu.selector.PunishMenu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;

import java.util.List;

public class PunishCommand extends BaseCommand<CoreUser, CoreData> {

    private final UserService<CoreUser, CoreData> userService;

    public PunishCommand(UserService<CoreUser, CoreData> userService) {
        super(List.of("punish"), Rank.TRIAL_MOD);
        this.userService = userService;
        setAsync(true);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (args.length != 1) {
            user.sendMessage(CC.RED + "Usage: /punish <player>");
            return;
        }
        userService.fetch(args[0]).ifPresentOrElse(target -> {
            if (user.isEqualOrAbove(Rank.MOD)) {
                new PunishMenu(user, target).open();
            } else {
                new MuteMenu(user, target).open();
            }
        }, () -> user.sendMessage(Lang.USER_NEVER_PLAYED));
    }
}
