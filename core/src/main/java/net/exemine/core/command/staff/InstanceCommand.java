package net.exemine.core.command.staff;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.instance.Instance;
import net.exemine.api.instance.InstanceService;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InstanceCommand extends BaseCommand<CoreUser, CoreData> {

    private final InstanceService instanceService;

    public InstanceCommand(InstanceService instanceService) {
        super(List.of("instance", "serverdata"), Rank.ADMIN, false);
        this.instanceService = instanceService;
        setUsage(CC.RED + "Usage: /instance <list|view|run> [instance|type] [command]");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(getUsage());
            return;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            sender.sendMessage("");
            sender.sendMessage(CC.PINK + "Lists of active instances:");

            String message = instanceService.getAllInstances()
                    .stream()
                    .sorted(Comparator.comparing(Instance::getName))
                    .map(this::getInfoMessage)
                    .collect(Collectors.joining("\n"));

            sender.sendMessage(message);
            sender.sendMessage("");
            return;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("view")) {
            Instance instance = instanceService.getInstance(args[1]);

            if (instance.isOffline()) {
                sender.sendMessage(CC.RED + "That instance has either timed out or it doesn't exist.");
                return;
            }
            sender.sendMessage("");
            sender.sendMessage(CC.PINK + "Showing you instance information:");
            sender.sendMessage(getInfoMessage(instance));
            sender.sendMessage("");
            return;
        }
        if (args.length >= 3 && args[0].equalsIgnoreCase("run")) {
            String command = StringUtil.join(args, 2);

            if (!isCommandAllowed(sender, command)) {
                sender.sendMessage(Lang.NO_PERMISSION);
                return;
            }
            String instanceName = args[1];

            if (instanceName.equals("all")) {
                instanceService.runCommand(command);
                sender.sendMessage(getRunMessage(command, CC.GOLD + "every" + CC.GRAY + " active instance."));
                return;
            }
            InstanceType type = InstanceType.get(args[1], false);

            if (type != null) {
                instanceService.runCommand(type, command);
                sender.sendMessage(getRunMessage(command, "all active " + CC.GOLD + type.name() + CC.GRAY + " instances."));
                return;
            }
            Instance instance = instanceService.getInstance(instanceName);

            if (instance.isOffline()) {
                sender.sendMessage(CC.RED + "That instance has either timed out or it doesn't exist.");
                return;
            }
            instanceService.runCommand(instance, command);
            sender.sendMessage(getRunMessage(command, "the " + CC.GOLD + instance.getName() + CC.GRAY + " instance."));
            return;
        }
        sender.sendMessage(getUsage());
    }

    private String getInfoMessage(Instance instance) {
        String separator = CC.GRAY + ", ";

        return Lang.LIST_PREFIX + CC.PURPLE + instance.getName()
                + separator + "Players: " + CC.WHITE + instance.getOnlinePlayers() + '/' + instance.getMaxPlayers()
                + separator + "TPS: " + formatTps(instance.getTps1()) + separator + formatTps(instance.getTps2()) + separator + formatTps(instance.getTps3())
                + separator + "Uptime: " + CC.WHITE + instance.getFormattedUptime();
    }

    private String getRunMessage(String command, String info) {
        return CC.PURPLE + "[Instance] " + CC.GRAY + "Command " + CC.GOLD + command + CC.GRAY + " was run on " + info;
    }

    private String formatTps(double tps) {
        if (tps > 20) tps = 20;
        String number = StringUtil.formatDecimalNumber(tps, 2);

        if (tps >= 19D) {
            return CC.WHITE + number;
        } else if (tps >= 18D) {
            return CC.RED + number;
        } else {
            return CC.DARK_RED + number + CC.BOLD + " (!)";
        }
    }

    private boolean isCommandAllowed(CommandSender sender, String command) {
        if (sender instanceof ConsoleCommandSender) return true;
        CoreUser user = userService.get(sender);
        return (commandService.canExecute(user, command) && Stream.of("setrank", "grant").noneMatch(command::startsWith))
                || user.isEqualOrAbove(Rank.OWNER);
    }
}
