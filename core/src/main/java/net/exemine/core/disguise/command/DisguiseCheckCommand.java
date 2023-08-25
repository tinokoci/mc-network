package net.exemine.core.disguise.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class DisguiseCheckCommand extends BaseCommand<CoreUser, CoreData> {

    public DisguiseCheckCommand() {
        super(List.of("disguisecheck", "dcheck"), Rank.TRIAL_MOD);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (args.length != 1) {
            user.sendMessage(CC.RED + "Usage: /disguisecheck <player>");
            return;
        }
        CoreUser target = userService.get(args[0]);
        if (isUserOffline(user, target)) return;

        if (!target.isDisguised()) {
            user.sendMessage(CC.RED + "That player is not disguised.");
            return;
        }
        user.sendMessage(CC.PURPLE + "[Disguise] " + CC.GRAY + "Nickname " + CC.GOLD + target.getBulkData().getDisguiseModel().getName()
         + CC.GRAY + " is a disguise for " + target.getColoredRealName() + CC.GRAY + '.');
    }
}
