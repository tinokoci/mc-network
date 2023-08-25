package net.exemine.core.command.staff;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.model.Motd;
import net.exemine.api.properties.PropertiesService;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class MotdCommand extends BaseCommand<CoreUser, CoreData> {

    private final PropertiesService propertiesService;

    public MotdCommand(PropertiesService propertiesService) {
        super(List.of("motd"), Rank.OWNER);
        this.propertiesService = propertiesService;
        setAsync(true);
        setUsage(CC.RED + "Usage: /motd [1|2] [text]");
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (args.length == 1) {
            user.sendMessage(getUsage());
            return;
        }
        Motd motd = propertiesService.getProperties().getMotd();

        if (args.length == 0) {
            user.sendMessage();
            user.sendMessage(CC.PINK + "Current network MOTD:");
            user.sendMessage(Lang.LIST_PREFIX + CC.RESET + CC.translate(motd.getLine1()));
            user.sendMessage(Lang.LIST_PREFIX + CC.RESET + CC.translate(motd.getLine2()));
            user.sendMessage();
            return;
        }
        if (motd == null) {
            motd = new Motd();
        }
        String text = StringUtil.join(args, 1);
        switch (args[0]) {
            case "1":
                if (motd.getLine1().equals(text)) {
                    user.sendMessage(CC.RED + "Line " + CC.BOLD + '1' + CC.RED + "of the motd is already set to: " + CC.RESET + CC.translate(text));
                    return;
                }
                motd.setLine1(text);
                propertiesService.update();
                user.sendMessage(CC.PURPLE + "[MOTD] " + CC.GRAY + "You've updated line " + CC.GOLD + '1' + CC.GRAY + " of the motd to: " + CC.RESET + CC.translate(text));
                break;
            case "2":
                if (motd.getLine2().equals(text)) {
                    user.sendMessage(CC.RED + "Line " + CC.BOLD + '2' + CC.RED + "of the motd is already set to: " + CC.RESET + CC.translate(text));
                    return;
                }
                motd.setLine2(text);
                propertiesService.update();
                user.sendMessage(CC.PURPLE + "[MOTD] " + CC.GRAY + "You've updated line " + CC.GOLD + '2' + CC.GRAY + " of the motd to: " + CC.RESET + CC.translate(text));
                break;
            default:
                user.sendMessage(getUsage());
        }
    }
}
