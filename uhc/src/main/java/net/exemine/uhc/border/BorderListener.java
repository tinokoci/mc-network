package net.exemine.uhc.border;

import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.lunar.impl.LunarBorder;
import net.exemine.core.util.LocationUtil;
import net.exemine.core.util.MessageUtil;
import net.exemine.uhc.UHC;
import net.exemine.uhc.assign.AutoAssignTask;
import net.exemine.uhc.border.event.BorderBuildEvent;
import net.exemine.uhc.border.event.BorderShrinkEvent;
import net.exemine.uhc.border.task.BorderShrinkTask;
import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.config.option.ToggleOption;
import net.exemine.uhc.location.LocationService;
import net.exemine.uhc.scenario.Scenario;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import net.exemine.uhc.world.WorldService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BorderListener implements Listener {

    private final UHC plugin;
    private final BorderService borderService;
    private final LocationService locationService;
    private final UHCUserService userService;
    private final WorldService worldService;

    public BorderListener(UHC plugin) {
        this.plugin = plugin;
        this.borderService = plugin.getBorderService();
        this.locationService = plugin.getLocationService();
        this.userService = plugin.getUserService();
        this.worldService = plugin.getWorldService();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBorderBuild(BorderBuildEvent event) {
        if (event.isCancelled()) return;
        borderService.buildBorder(event.getBorderRadius());
    }

    /*@EventHandler(priority = EventPriority.LOWEST)
    public void onBorderShrinkLOWEST(BorderShrinkEvent event) {
        BorderRadius radius = event.getBorderRadius();
        if (!radius.isEqualOrLower(BorderRadius.RADIUS_50)) return;

        int teamSize = NumberOption.PLAYERS_PER_TEAM.getValue();
        int playing = userService.getInGameUsers().size();

        int playersForShrink = teamSize == 1 ? (radius == BorderRadius.RADIUS_50 ? 8 : 4)
                : teamSize == 2 ? (radius == BorderRadius.RADIUS_50 ? 16 : 8)
                : (radius == BorderRadius.RADIUS_50 ? 18 : 12);

        if (playing > playersForShrink) {
            event.setCancelled(true);
            MessageUtil.send(CC.RED + "The border shrink has been cancelled because there is too many players. " + CC.GRAY + "(Delaying shrink by 2 minutes.)");
            new BorderShrinkTask(plugin, radius, 120);
        }
    }*/

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBorderShrinkHIGHEST(BorderShrinkEvent event) {
        if (event.isCancelled()) return;

        BorderRadius borderRadius = event.getBorderRadius();
        shrinkPlayersToBorderRadius(borderRadius);

        World world = worldService.getUhcWorld();
        world.getPlayers().forEach(player -> new LunarBorder(world, borderRadius.getValue()).send(player));

        if (!borderService.isFirstShrinkOccurred()) {
            teleportPlayersOutOfNether();
        }
        switch (borderRadius) {
            case RADIUS_100:
                Scenario.DO_NOT_DISTURB.enable();
                Scenario.NO_CLEAN.enable();
                Scenario.SAFE_LOOT.enable();

                MessageUtil.send("");
                MessageUtil.send(CC.PINK + "Welcome to the " + CC.BOLD_PURPLE + borderRadius + "x" + borderRadius + CC.PINK + " border.");
                MessageUtil.send(Lang.LIST_PREFIX + CC.GOLD + " Camping isn't allowed.");
                MessageUtil.send(Lang.LIST_PREFIX + CC.GOLD + " Sky bases aren't allowed.");
                MessageUtil.send(Lang.LIST_PREFIX + CC.GOLD + " Mining/hiding under ground isn't allowed.");
                MessageUtil.send(Lang.LIST_PREFIX + CC.GOLD + " Do Not Disturb, No Clean and Safe Loot are now enabled.");
                MessageUtil.send(CC.RED + "Ignoring these rules will lead to a punishment.");
                MessageUtil.send("");

                worldService.clearEntities(WorldService.ClearType.HARD);
                break;
            case RADIUS_50:
                if (ToggleOption.AUTO_ASSIGN.isEnabled()) {
                    MessageUtil.send("");
                    MessageUtil.send(CC.PINK + "Welcome to the " + CC.BOLD_PURPLE + borderRadius + "x" + borderRadius + CC.PINK + " border.");
                    MessageUtil.send(CC.GOLD + "From now on every fight will be automatically assigned.");
                    MessageUtil.send("");
                    new AutoAssignTask(plugin, true);
                }
        }
    }

    private void shrinkPlayersToBorderRadius(BorderRadius borderRadius) {
        int radiusValue = borderRadius.getValue();
        World world = worldService.getUhcWorld();
        HashMap<Integer, Location> teamLocations = new HashMap<>();

        userService.getOnlineUsers()
                .stream()
                .filter(user -> LocationUtil.isOutsideRadius(user, radiusValue))
                .forEach(user -> {
                    Location teleportLocation;
                    Location userLocation = user.getLocation();

                    // Random Teleport
                    if (ToggleOption.RANDOM_TELEPORT.isEnabled() || borderRadius.isEqual(BorderRadius.RADIUS_100)) {
                        int teamId = user.getTeam().getId();

                        if (!teamLocations.containsKey(teamId)) {
                            teamLocations.put(teamId, locationService.getGameScatterLocation(borderRadius));
                        }
                        teleportLocation = teamLocations.get(user.getTeam().getId());
                    }
                    // '97 Teleport
                    else {
                        double x = userLocation.getBlockX() > radiusValue ? radiusValue - 2.5
                                : userLocation.getBlockX() < -radiusValue ? -radiusValue + 3.5
                                : userLocation.getX();
                        double z = userLocation.getBlockZ() > radiusValue ? radiusValue - 2.5
                                : userLocation.getBlockZ() < -radiusValue ? -radiusValue + 3.5
                                : userLocation.getZ();
                        teleportLocation = new Location(world, x, world.getHighestBlockYAt((int) x, (int) z) + 0.5, z);
                    }
                    teleportLocation.setYaw(userLocation.getYaw());
                    teleportLocation.setPitch(userLocation.getPitch());

                    user.teleportWithVehicle(teleportLocation);
                    user.setInvulnerableTicks(20);
                    user.sendMessage(CC.RED + "You were shrunk in the border.");
                });
    }

    private void teleportPlayersOutOfNether() {
        HashMap<Integer, Location> teamLocations = new HashMap<>();
        List<UHCUser> usersToTeleport = new ArrayList<>();

        userService.getOnlineUsers()
                .stream()
                .filter(UHCUser::isInNether)
                .forEach(user -> {
                    usersToTeleport.add(user);
                    int teamId = user.getTeam().getId();

                    if (!teamLocations.containsKey(teamId)) {
                        World world = worldService.getUhcWorld();
                        double x = user.getLocation().getX() * 1.5;
                        double z = user.getLocation().getZ() * 1.5;
                        Location location = new Location(world, x, world.getHighestBlockYAt((int) x, (int) z) + 0.5, z);
                        teamLocations.put(teamId, location);
                    }
                });
        usersToTeleport
                .stream()
                .filter(UHCUser::isOnline)
                .forEach(user -> {
                    user.teleportWithVehicle(teamLocations.get(user.getTeam().getId()));
                    user.setInvulnerableTicks(20);
                    user.sendMessage(CC.RED + "You were teleported out of the nether.");
                });
    }
}
