package net.exemine.core.command.staff;

import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.permission.PermissionService;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;
import org.bukkit.command.CommandSender;

import java.util.List;

public class PermissionCommand extends BaseCommand<CoreUser, CoreData> {

    private final PermissionService permissionService;

    public PermissionCommand(PermissionService permissionService) {
        super(List.of("permission", "permissions"), Rank.ADMIN, false);
        this.permissionService = permissionService;

        setAsync(true);
        setUsage(CC.RED + "Usage: /permission <add|remove|list|clear> <player> [node] [duration]");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 2 && args.length != 3 && args.length != 4) {
            sender.sendMessage(getUsage());
            return;
        }
        userService.fetch(args[1]).ifPresentOrElse(target -> {
            BulkData data = target.getBulkData();
            String node;

            switch (args[0].toLowerCase()) {
                case "add":
                    if (args.length != 4) {
                        sender.sendMessage(getUsage());
                        return;
                    }
                    node = args[2].toLowerCase();
                    long duration = TimeUtil.getMillisFromInput(args[3]);

                    permissionService.addPermission(data, node, duration);
                    sender.sendMessage(CC.PURPLE + "[Permission] " + CC.GRAY + "You have added " + CC.GOLD + node + CC.GRAY + " node to " + target.getColoredRealName() + CC.GRAY + '.');
                    break;
                case "remove":
                    if (args.length != 3) {
                        sender.sendMessage(getUsage());
                        return;
                    }
                    node = args[2].toLowerCase();

                    if (!data.hasPermission(node)) {
                        sender.sendMessage(target.getColoredRealName() + CC.RED + " doesn't have " + CC.BOLD + node + CC.RED + " node.");
                        return;
                    }
                    permissionService.removePermission(data, node);
                    sender.sendMessage(CC.PURPLE + "[Permission] " + CC.GRAY + "You've removed " + CC.GOLD + node + CC.GRAY + " node from " + target.getColoredRealName() + CC.GRAY + '.');
                    break;
                case "list":
                    if (args.length != 2) {
                        sender.sendMessage(getUsage());
                        return;
                    }
                    if (data.getActivePermissionList().isEmpty()) {
                        sender.sendMessage(target.getColoredRealName() + CC.RED + " doesn't have any permissions.");
                        return;
                    }
                    sender.sendMessage(CC.PINK + "Lists of " + target.getColoredRealName() + CC.PINK + "'s permissions:");
                    data.getActivePermissionList().forEach(p -> sender.sendMessage(Lang.LIST_PREFIX + CC.PURPLE + p.getNode() + CC.GRAY + " (" + p.getFormattedDuration() + CC.GRAY + ')'));
                    break;
                case "clear":
                    if (args.length != 2) {
                        sender.sendMessage(getUsage());
                        return;
                    }
                    if (data.getActivePermissionList().isEmpty()) {
                        sender.sendMessage(target.getColoredRealName() + CC.RED + " doesn't have any permissions.");
                        return;
                    }
                    permissionService.clearPermissions(data);
                    sender.sendMessage(CC.PURPLE + "[Permission] " + CC.GRAY + "You have cleared " + target.getColoredRealName() + CC.GRAY + "'s permissions.");
                    break;
                default:
                    sender.sendMessage(getUsage());
            }
        }, () -> sender.sendMessage(Lang.USER_NEVER_PLAYED));
    }
}
