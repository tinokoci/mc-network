package net.exemine.core.command.staff;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;
import org.bukkit.command.CommandSender;

import java.util.List;

public class BroadcastMeCommand extends BaseCommand<CoreUser, CoreData> {

    public BroadcastMeCommand() {
        super(List.of("broadcastme", "bcme"), Rank.ADMIN, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(CC.RED + "Usage: /broadcastme <text>");
            return;
        }
        String message = StringUtil.join(args);
        sender.sendMessage(CC.translate(message));
    }
}
