package net.exemine.hub.command;

import net.exemine.api.data.impl.HubData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.hub.location.LocationService;
import net.exemine.hub.user.HubUser;

import java.util.List;

public class LocationCommand extends BaseCommand<HubUser, HubData> {

    private final LocationService locationService;

    public LocationCommand(LocationService locationService) {
        super(List.of("location", "locations"), Rank.DEVELOPER);
        this.locationService = locationService;
        setUsage(CC.RED + "Usage: /location <spawn|uhc|unknown|welcome>");
    }

    @Override
    public void execute(HubUser user, HubData data, String[] args) {
        if (args.length != 1) {
            user.sendMessage(getUsage());
            return;
        }
        switch (args[0].toLowerCase()) {
            case "spawn":
                locationService.updateSpawnLocation(user.getLocation());
                user.sendMessage(getSetupMessage("Spawn"));
                break;
            case "uhc":
                locationService.updateUhcNpcLocation(user.getLocation());
                user.sendMessage(getSetupMessage("UHC NPC"));
                break;
            case "unknown":
                locationService.updateUnknownNpcLocation(user.getLocation());
                user.sendMessage(getSetupMessage("Unknown NPC"));
                break;
            case "welcome":
                locationService.updateWelcomeHologram(user.getLocation());
                user.sendMessage(getSetupMessage("Welcome Hologram"));
                break;
            default:
                user.sendMessage(getUsage());
        }
    }

    private String getSetupMessage(String name) {
        return CC.PURPLE + "[Location] " + CC.GRAY + "You have setup a new " + CC.GOLD + name + CC.GRAY + " location.";
    }
}
