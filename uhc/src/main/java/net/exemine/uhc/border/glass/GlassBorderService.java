package net.exemine.uhc.border.glass;

import com.execets.spigot.ExeSpigot;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.MathUtil;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;
import net.exemine.core.util.LocationUtil;
import net.exemine.uhc.border.BorderService;
import net.exemine.uhc.border.glass.interceptor.GlassBorderMoveInterceptor;
import net.exemine.uhc.border.glass.interceptor.GlassBorderPacketInterceptor;
import net.exemine.uhc.location.LocationService;
import net.exemine.uhc.world.WorldService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GlassBorderService implements Listener {

    private static final int BORDER_DISTANCE_TRIGGER = 15;

    private static final int Y_OFFSET_MIN = -4;
    private static final int Y_OFFSET_MAX = 5;
    private static final int XZ_OFFSET_MIN = -5;
    private static final int XZ_OFFSET_MAX = 6;

    public GlassBorderService(BorderService borderService, LocationService locationService, WorldService worldService, UserService<CoreUser, CoreData> userService) {
        ExeSpigot.INSTANCE.addMovementHandler(new GlassBorderMoveInterceptor(borderService, this, locationService, worldService, userService));
        ExeSpigot.INSTANCE.addPacketHandler(new GlassBorderPacketInterceptor(this, userService));
    }

    private final HashMap<UUID, Set<Location>> locations = new HashMap<>();
    private final Set<Material> passThrough = new HashSet<Material>() {{
        add(Material.AIR);
        add(Material.WATER);
        add(Material.STATIONARY_WATER);
        add(Material.LAVA);
        add(Material.STATIONARY_LAVA);
        add(Material.VINE);
        add(Material.SNOW);
        add(Material.LONG_GRASS);
        add(Material.DOUBLE_PLANT);
    }};

    public void updateLocations(Player player, int radius) {
        HashSet<Location> set = new HashSet<>();
        Location userLocation = player.getLocation();

        int closestX = MathUtil.getClosest(userLocation.getBlockX(), radius);
        int closestZ = MathUtil.getClosest(userLocation.getBlockZ(), radius);

        boolean spawnBorderOnXBorder = MathUtil.getDistance(userLocation.getBlockX(), closestX) < BORDER_DISTANCE_TRIGGER;
        boolean spawnBorderOnZBorder = MathUtil.getDistance(userLocation.getBlockZ(), closestZ) < BORDER_DISTANCE_TRIGGER;

        if (!spawnBorderOnXBorder && !spawnBorderOnZBorder) {
            restoreBlocks(player);
            return;
        }
        if (spawnBorderOnXBorder) {
            for (int yOffset = Y_OFFSET_MIN; yOffset < Y_OFFSET_MAX; yOffset++) {
                for (int zOffset = XZ_OFFSET_MIN; zOffset < XZ_OFFSET_MAX; zOffset++) {
                    Location Location = new Location(userLocation.getWorld(), closestX, userLocation.getBlockY() + yOffset, userLocation.getBlockZ() + zOffset);
                    if (!set.contains(Location)
                            && passThrough.contains(Location.getBlock().getType())
                            && LocationUtil.isInsideRadius(Location, radius)) {
                        set.add(Location);
                    }
                }
            }
        }
        if (spawnBorderOnZBorder) {
            for (int yOffset = Y_OFFSET_MIN; yOffset < Y_OFFSET_MAX; yOffset++) {
                for (int xOffset = XZ_OFFSET_MIN; xOffset < XZ_OFFSET_MAX; xOffset++) {
                    Location location = new Location(userLocation.getWorld(), userLocation.getBlockX() + xOffset, userLocation.getBlockY() + yOffset, closestZ);
                    if (!set.contains(location)
                            && passThrough.contains(location.getBlock().getType())
                            && LocationUtil.isInsideRadius(location, radius)) {
                        set.add(location);
                    }
                }
            }
        }
        renderGlassBorder(player, set);
    }

    public void sendGlassBlockUpdate(Player player, Location location) {
        player.sendBlockChange(location, 95, (byte) 1);
    }

    private void renderGlassBorder(Player player, Set<Location> set) {
        Set<Location> previousLocations = locations.get(player.getUniqueId());

        if (previousLocations != null) {
            previousLocations.addAll(set);
            previousLocations
                    .stream()
                    .filter(location -> !set.contains(location))
                    .forEach(location -> {
                        Block block = location.getBlock();
                        player.sendBlockChange(location, block.getTypeId(), block.getData());
                    });
        }
        set.forEach(location -> sendGlassBlockUpdate(player, location));
        locations.put(player.getUniqueId(), set);
    }

    private void restoreBlocks(Player player) {
        if (!locations.containsKey(player.getUniqueId())) return;

        locations.get(player.getUniqueId()).forEach(location -> {
            Block block = location.getBlock();
            player.sendBlockChange(location, block.getTypeId(), block.getData());
        });
        locations.remove(player.getUniqueId());
    }

    public boolean hasGlassAt(Player player, Location location) {
        return locations.getOrDefault(player.getUniqueId(), new HashSet<>()).contains(location);
    }
}
