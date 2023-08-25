package net.exemine.core.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.link.LinkService;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class UnlinkCommand extends BaseCommand<CoreUser, CoreData> {

    private final LinkService linkService;

    public UnlinkCommand(LinkService linkService) {
        super(List.of("unlink"));
        this.linkService = linkService;
        setAsync(true);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (!data.isDiscordLinked()) {
            user.sendMessage(CC.RED + "You are not linked to a discord account.");
            return;
        }
        linkService.unlinkAccount(data);
        user.sendMessage(CC.PURPLE + "[Link] " + CC.GRAY + "You are no longer linked to a discord account.");
    }
}
