package net.exemine.uhc.command.staff;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.user.UHCUser;

import java.util.List;

public class TeleportHereCommand extends BaseCommand<UHCUser, UHCData> {

    public TeleportHereCommand() {
        super(List.of("teleporthere", "tphere"), Rank.TRIAL_MOD);
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (args.length != 1) {
            user.sendMessage(CC.RED + "Usage: /tphere <player>");
            return;
        }
        user.performCommand("teleport " + args[0] + ' ' + user.getRealName());
    }
}
