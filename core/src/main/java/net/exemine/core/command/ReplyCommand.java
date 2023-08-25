package net.exemine.core.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class ReplyCommand extends BaseCommand<CoreUser, CoreData> {

    public ReplyCommand() {
        super(List.of("reply", "r"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (args.length == 0) {
            user.sendMessage(CC.RED + "Usage: /reply <message>");
            return;
        }
        CoreUser target = userService.get(user.getConversationPartner());

        if (target == null) {
            user.sendMessage(CC.RED + "You are not in a conversation or the player went offline.");
            return;
        }
        user.sendPrivateMessage(target, StringUtil.join(args, 0));
    }
}