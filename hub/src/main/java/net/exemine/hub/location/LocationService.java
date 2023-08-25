package net.exemine.hub.location;

import lombok.Getter;
import net.exemine.api.util.config.ConfigFile;
import net.exemine.core.nms.hologram.Hologram;
import net.exemine.core.nms.npc.NPC;
import net.exemine.core.util.LocationUtil;
import net.exemine.hub.nms.NMSService;
import org.bukkit.Location;

@Getter
public class LocationService {

    private final ConfigFile config;
    private NMSService nmsService;

    private Location spawnLocation;
    private Location uhcNpcLocation;
    private Location ffaNpcLocation;
    private Location welcomeHologramLocation;

    public LocationService(ConfigFile config) {
        this.config = config;

        spawnLocation = LocationUtil.deserializeLocation(config.getString("location.spawn"));
        uhcNpcLocation = LocationUtil.deserializeLocation(config.getString("location.npc.uhc"));
        ffaNpcLocation = LocationUtil.deserializeLocation(config.getString("location.npc.unknown"));
        welcomeHologramLocation = LocationUtil.deserializeLocation(config.getString("location.hologram.welcome"));
    }

    public void init(NMSService nmsService) {
        this.nmsService = nmsService;
    }

    public void updateSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
        updateLocationInConfigFile("location.spawn", spawnLocation);
    }

    public void updateUhcNpcLocation(Location uhcNpcLocation) {
        this.uhcNpcLocation = uhcNpcLocation;
        updateNpcLocation(uhcNpcLocation, nmsService.getUhcNpc(), "uhc");
    }

    public void updateUnknownNpcLocation(Location uknownNpcLocation) {
        this.ffaNpcLocation = uknownNpcLocation;
        updateNpcLocation(ffaNpcLocation, nmsService.getFfaNpc(), "unknown");
    }

    private void updateNpcLocation(Location location, NPC npc, String key) {
        if (npc != null) {
            npc.setLocation(location);
        }
        updateLocationInConfigFile("location.npc." + key, location);
    }

    public void updateWelcomeHologram(Location welcomeHologramLocation) {
        this.welcomeHologramLocation = welcomeHologramLocation;
        Hologram welcomeHologram = nmsService.getWelcomeHologram();

        if (welcomeHologram != null) {
            welcomeHologram.setLocation(welcomeHologramLocation);
        }
        updateLocationInConfigFile("location.hologram.welcome", welcomeHologramLocation);
    }

    private void updateLocationInConfigFile(String key, Location location) {
        config.set(key, LocationUtil.serializeLocation(location));
        config.save();
    }
}
