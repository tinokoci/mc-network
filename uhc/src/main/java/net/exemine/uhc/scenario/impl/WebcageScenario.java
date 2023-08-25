package net.exemine.uhc.scenario.impl;

import net.exemine.uhc.scenario.ScenarioListener;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;

public class WebcageScenario extends ScenarioListener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UHCUser user = plugin.getUserService().retrieve(player.getUniqueId()); // using retrieve because of combat logger
        if (!user.isPlaying()) return;

        getSphereLocations(player.getLocation(), 5, true)
                .stream()
                .filter(webLocation -> webLocation.getBlock().getType() == Material.AIR)
                .forEach(webLocation -> webLocation.getBlock().setType(Material.WEB));
    }

    public List<Location> getSphereLocations(Location center, int radius, boolean hollow) {
        List<Location> sphereLocations = new ArrayList<>();

        int bx = center.getBlockX();
        int by = center.getBlockY();
        int bz = center.getBlockZ();

        for (int x = bx - radius; x <= bx + radius; x++) {
            for (int y = by - radius; y <= by + radius; y++) {
                for (int z = bz - radius; z <= bz + radius; z++) {
                    double distance = ((bx - x) * (bx - x) + ((bz - z) * (bz - z)) + ((by - y) * (by - y)));
                    if (distance < radius * radius && !(hollow && distance < ((radius - 1) * (radius - 1)))) {
                        Location location = new Location(center.getWorld(), x, y, z);
                        sphereLocations.add(location);
                    }
                }
            }
        }
        return sphereLocations;
    }

}
