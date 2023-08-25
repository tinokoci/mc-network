package net.exemine.core.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class PingCommand extends BaseCommand<CoreUser, CoreData> {

    public PingCommand() {
        super(List.of("ping", "latency"));
    }

    @Override public void execute(CoreUser user, CoreData data, String[] args) {
        CoreUser target = args.length == 0
                ? user
                : userService.get(args[0]);
        if (isUserOffline(user, target)) return;

        int ping = target.getPing();
        String color = ping < 100 ? CC.GREEN
                : ping < 350 ? CC.YELLOW
                : CC.RED;
        user.sendMessage((user == target ? CC.GOLD + "Ping: " : target.getColoredDisplayName() + CC.GOLD + "'s ping: ") + color + ping + "ms");
    }
}
