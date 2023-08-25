package net.exemine.uhc.command.staff;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.location.LocationService;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.Location;

import java.util.List;
import java.util.stream.Stream;

public class LocationCommand extends BaseCommand<UHCUser, UHCData> {

    private final LocationService locationService;

    public LocationCommand(LocationService locationService) {
        super(List.of("location", "locations"), Rank.ADMIN);
        this.locationService = locationService;
        setUsage(CC.RED + "Usage: /location <set|tp> spawn");
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (args.length != 2 || Stream.of("set", "tp").noneMatch(arg0 -> arg0.equalsIgnoreCase(args[0]))) {
            user.sendMessage(getUsage());
            return;
        }
        boolean set = args[0].equalsIgnoreCase("set");

        switch (args[1].toLowerCase()) {
            case "spawn":
                if (set) {
                    locationService.updateSpawnLocation(user.getLocation());
                    user.sendMessage(CC.PURPLE + "[Location] " + CC.GRAY + "You've updated the " + CC.GOLD + "spawn" + CC.GRAY + " location.");
                    return;
                }
                Location spawnLocation = locationService.getLobbySpawnLocation();

                if (spawnLocation == null) {
                    user.sendMessage(CC.RED + "Location for " + CC.BOLD + "spawn" + CC.RED + " is not set.");
                    return;
                }
                user.teleport(locationService.getLobbySpawnLocation());
                user.sendMessage(CC.PURPLE + "[Location] " + CC.GRAY + "You've teleported to the " + CC.GOLD + "spawn" + CC.GRAY + " location.");
                break;
            default:
                user.sendMessage(getUsage());
        }
    }
}
