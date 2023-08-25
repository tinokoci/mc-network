package net.exemine.core.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class MessageCommand extends BaseCommand<CoreUser, CoreData> {

    public MessageCommand() {
        super(List.of("message", "msg", "m", "tell", "w", "whisper"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (args.length < 2) {
            user.sendMessage(CC.RED + "Usage: /msg <player> <message>");
            return;
        }
        CoreUser target = userService.get(args[0]);
        if (isUserOffline(user, target)) return;

        if (target == user) {
            user.sendMessage(CC.RED + "You cannot message yourself.");
            return;
        }
        user.sendPrivateMessage(target, StringUtil.join(args, 1));
    }
}