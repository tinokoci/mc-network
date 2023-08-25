package net.exemine.core.disguise.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.disguise.DisguiseService;
import net.exemine.core.disguise.entry.DisguiseEntryType;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class DisguiseAdminCommand extends BaseCommand<CoreUser, CoreData> {

    private final DisguiseService disguiseService;

    public DisguiseAdminCommand(DisguiseService disguiseService) {
        super(List.of("disguiseadmin"), Rank.DEVELOPER);
        this.disguiseService = disguiseService;
        setAsync(true);
        setUsage(CC.RED + "Usage: /disguiseadmin <name|skin> <add|remove> <playerName>");
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (args.length != 3) {
            user.sendMessage(getUsage());
            return;
        }
        DisguiseEntryType entryType = DisguiseEntryType.get(args[0]);

        if (entryType == null) {
            user.sendMessage(getUsage());
            return;
        }
        String entryTypeName = entryType.name().toLowerCase();
        String value = args[2];

        switch (args[1].toLowerCase()) {
            case "add":
                if (disguiseService.hasValue(entryType, value)) {
                    user.sendMessage(CC.RED + StringUtil.capitalize(entryTypeName) + " list already has " + CC.BOLD + value + CC.RED + " value.");
                    return;
                }
                disguiseService.addValue(entryType, value);
                user.sendMessage(CC.PURPLE + "[Disguise] " + CC.GRAY + "You have added " + CC.GOLD + value + CC.GRAY + " to the " + entryTypeName + " list.");
                break;
            case "remove":
                if (!disguiseService.hasValue(entryType, value)) {
                    user.sendMessage(CC.RED + StringUtil.capitalize(entryTypeName) + " list doesn't have " + CC.BOLD + value + CC.RED + " value.");
                    return;
                }
                disguiseService.removeValue(entryType, value);
                user.sendMessage(CC.PURPLE + "[Disguise] " + CC.GRAY + "You have removed " + CC.GOLD + value + CC.GRAY + " from the " + entryTypeName + " list.");
                break;
            default:
                user.sendMessage(getUsage());
        }
    }
}
