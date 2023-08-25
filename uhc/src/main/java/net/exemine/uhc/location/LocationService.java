package net.exemine.uhc.location;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.util.config.ConfigFile;
import net.exemine.core.util.LocationUtil;
import net.exemine.uhc.UHC;
import net.exemine.uhc.border.BorderRadius;
import net.exemine.uhc.world.WorldService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class LocationService {

    private final UHC plugin;
    private final ConfigFile config;
    private final WorldService worldService;

    private Location lobbySpawnLocation;

    private final int lobbyBoundXMin;
    private final int lobbyBoundXMax;
    private final int lobbyBoundZMin;
    private final int lobbyBoundZMax;
    private final int lobbyBoundYMax;

    private final int lobbyWhooshY;

    private final int practiceScatterXZBound;
    private final int practiceScatterYBound;
    private final int practiceMapRadius;

    private final List<Point> highestLocationPoints = new ArrayList<>();
    private final Map<Point, Integer> highestLocations = new HashMap<>();

    public LocationService(UHC plugin) {
        this.plugin = plugin;
        config = plugin.getConfigFile();
        worldService = plugin.getWorldService();

        lobbySpawnLocation = LocationUtil.deserializeLocation(plugin.getConfigFile().getString("location.spawn"));
        lobbyBoundXMin = config.getInt("lobby_bound.x.min");
        lobbyBoundXMax = config.getInt("lobby_bound.x.max");
        lobbyBoundZMin = config.getInt("lobby_bound.z.min");
        lobbyBoundZMax = config.getInt("lobby_bound.z.max");
        lobbyBoundYMax = config.getInt("lobby_bound.y.max");
        lobbyWhooshY = config.getInt("lobby_whoosh.y");
        practiceScatterXZBound = config.getInt("practice_scatter_xz.bound");
        practiceScatterYBound = config.getInt("practice_scatter_y.bound");
        practiceMapRadius = config.getInt("practice_map_radius");

        loadHighestLocations();
    }

    public void loadHighestLocations() {
        // Lobby
        for (int x = lobbyBoundXMin; x < lobbyBoundXMax; x++) {
            for (int z = lobbyBoundZMin; z < lobbyBoundZMax; z++) {
                for (int y = lobbyBoundYMax; y > 0; y--) {
                    Block block = worldService.getLobbyWorld().getBlockAt(x, y, z);
                    if (block.getType().isSolid()) {
                        Point point = new Point(worldService.getLobbyWorld(), x, z);
                        highestLocationPoints.add(point);
                        highestLocations.put(point, y);
                        break;
                    }
                }
            }
        }
        // Practice
        for (int x = -practiceScatterXZBound; x < practiceScatterXZBound + 1; x++) {
            for (int z = -practiceScatterXZBound; z < practiceScatterXZBound + 1; z++) {
                for (int y = practiceScatterYBound; y > 0; y--) {
                    Block block = worldService.getPracticeWorld().getBlockAt(x, y, z);
                    if (block.getType().isSolid()) {
                        Point point = new Point(worldService.getPracticeWorld(), x, z);
                        highestLocationPoints.add(point);
                        highestLocations.put(point, y);
                        break;
                    }
                }
            }
        }
    }

    public int getHighestYInWorld(World world, int x, int z) {
        Point point = highestLocationPoints
                .stream()
                .filter(p -> p.getX() == x && p.getZ() == z && p.getWorld() == world)
                .findFirst()
                .orElse(null);
        if (point == null) return -1;
        return highestLocations.get(point);
    }

    public Location getHighestLocationInWorld(World world, int x, int z) {
        return new Location(world, x, getHighestYInWorld(world, x, z), z);
    }

    public Location getGameScatterLocation() {
        return getGameScatterLocation(plugin.getBorderService().getCurrentRadius());
    }

    public Location getGameScatterLocation(BorderRadius borderRadius) {
        int radiusValue = borderRadius.getValue();
        int x = ThreadLocalRandom.current().nextInt(-radiusValue + 10, radiusValue - 10);
        int z = ThreadLocalRandom.current().nextInt(-radiusValue + 10, radiusValue - 10);

        World world = worldService.getUhcWorld();
        Block block = world.getHighestBlockAt(x, z);
        Material downBlockMaterial = block.getRelative(BlockFace.DOWN).getType();

        if (block.getLocation().getY() < 40 || downBlockMaterial.name().endsWith("WATER") || downBlockMaterial.name().endsWith("LAVA")) {
            return getGameScatterLocation();
        }
        return block.getLocation().add(0, 0.5, 0);
    }

    public Location getPracticeScatterLocation() {
        int x = ThreadLocalRandom.current().nextInt(-practiceScatterXZBound, practiceScatterXZBound + 1);
        int z = ThreadLocalRandom.current().nextInt(-practiceScatterXZBound, practiceScatterXZBound + 1);
        int y = getHighestYInWorld(worldService.getPracticeWorld(), x, z);

        Location location = new Location(worldService.getPracticeWorld(), x, y + 15, z);
        if (location.getY() > 70 || location.getY() == -1) return getPracticeScatterLocation();

        return location;
    }

    public void updateSpawnLocation(Location spawnLocation) {
        this.lobbySpawnLocation = spawnLocation;
        config.set("location.spawn", LocationUtil.serializeLocation(spawnLocation));
        config.save();
    }

    @RequiredArgsConstructor
    @Getter
    public static class Point {

        private final World world;
        private final int x;
        private final int z;
    }
}
