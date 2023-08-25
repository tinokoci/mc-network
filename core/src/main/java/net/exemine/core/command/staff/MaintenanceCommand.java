package net.exemine.core.command.staff;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.properties.Properties;
import net.exemine.api.properties.PropertiesService;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MaintenanceCommand extends BaseCommand<CoreUser, CoreData> {

    private final PropertiesService propertiesService;

    public MaintenanceCommand(PropertiesService propertiesService) {
        super(List.of("maintenance"), Rank.ADMIN, false);
        this.propertiesService = propertiesService;
        setAsync(true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sendUsage(sender);
            return;
        }
        Rank maintenanceRank = Rank.get(args[0]);

        if (maintenanceRank == null) {
            sendUsage(sender);
            return;
        }
        if (sender instanceof Player && !userService.get(sender).isEqualOrAbove(maintenanceRank)) {
            sender.sendMessage(CC.RED + "You cannot set that maintenance rank because you don't have a high enough rank.");
            return;
        }
        Properties properties = propertiesService.getProperties();
        if (properties.getMaintenanceRank().isEqual(maintenanceRank)) {
            sender.sendMessage(CC.RED + "The network is already under the " + CC.BOLD + maintenanceRank.getName() + CC.RED + " maintenance.");
            return;
        }
        properties.setMaintenanceRank(maintenanceRank);
        propertiesService.update();
        sender.sendMessage(CC.PURPLE + "[Maintenance] " + CC.GRAY + "Only " + CC.GOLD + maintenanceRank.getDisplayName() + CC.GRAY + " and higher ranked players can now join the network.");
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(CC.RED + "Usage: /maintenance <type>");
        sender.sendMessage(CC.RED + "Current: " + CC.BOLD + propertiesService.getProperties().getMaintenanceRank().getName());
        sender.sendMessage(CC.RED + "Types: " + Arrays.stream(Rank.values())
                .map(Rank::name)
                .collect(Collectors.joining(", ")));
    }
}
