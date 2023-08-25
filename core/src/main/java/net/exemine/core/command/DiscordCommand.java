package net.exemine.core.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class DiscordCommand extends BaseCommand<CoreUser, CoreData> {

    public DiscordCommand() {
        super(List.of("discord"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        user.sendMessage(CC.PURPLE + "[Exemine] " + CC.GRAY + "Discord: " + CC.GOLD + Lang.DISCORD);
    }
}
