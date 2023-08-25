package net.exemine.core.command.staff;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class LinkLockCommand extends BaseCommand<CoreUser, CoreData> {

    public LinkLockCommand() {
        super(List.of("linklock"), Rank.OWNER);
        setAsync(true);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (args.length != 1) {
            user.sendMessage(CC.RED + "Usage: /linklock <player>");
            return;
        }
        userService.fetch(args[0]).ifPresentOrElse(target -> {
            CoreData targetData = target.getData();
            targetData.setDiscordLocked(!targetData.isDiscordLocked());
            target.saveData(false);

            if (targetData.isDiscordLinked()) {
                target.sendDiscordUpdate();
            }
            user.sendMessage(CC.PURPLE + "[Link] " + target.getColoredRealName() + CC.GRAY + " is "
                    + StringUtil.formatBooleanCommand(targetData.isDiscordLocked()) + CC.GRAY + " link locked.");
        }, () -> user.sendMessage(Lang.USER_NEVER_PLAYED));
    }
}
