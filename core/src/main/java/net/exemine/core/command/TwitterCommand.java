package net.exemine.core.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class TwitterCommand extends BaseCommand<CoreUser, CoreData> {

    public TwitterCommand() {
        super(List.of("twitter"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        user.sendMessage();
        user.sendMessage(CC.PINK + "Our official Twitter accounts:");
        user.sendMessage(Lang.LIST_PREFIX + CC.WHITE + Lang.TWITTER);
        user.sendMessage(Lang.LIST_PREFIX + CC.WHITE + Lang.UHC_FEED_TWITTER);
        user.sendMessage();
    }
}
