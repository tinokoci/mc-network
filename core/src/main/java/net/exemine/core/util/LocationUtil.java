package net.exemine.core.util;

import net.exemine.core.user.base.ExeUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class LocationUtil {

    public static Location deserializeLocation(String input) {
        if (input == null) return null;

        String[] attributes = input.split(":");

        World world = null;
        Double x = null;
        Double y = null;
        Double z = null;
        Float pitch = null;
        Float yaw = null;

        for (String attribute : attributes) {
            String[] split = attribute.split(";");

            if (split[0].equalsIgnoreCase("#w")) {
                world = Bukkit.getWorld(split[1]);
                continue;
            }
            if (split[0].equalsIgnoreCase("#x")) {
                x = Double.parseDouble(split[1]);
                continue;
            }
            if (split[0].equalsIgnoreCase("#y")) {
                y = Double.parseDouble(split[1]);
                continue;
            }
            if (split[0].equalsIgnoreCase("#z")) {
                z = Double.parseDouble(split[1]);
                continue;
            }
            if (split[0].equalsIgnoreCase("#p")) {
                pitch = Float.parseFloat(split[1]);
                continue;
            }
            if (split[0].equalsIgnoreCase("#yaw")) {
                yaw = Float.parseFloat(split[1]);
            }
        }
        if (world == null || x == null || y == null || z == null || pitch == null || yaw == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static String serializeLocation(Location location) {
        return "#w;" + location.getWorld().getName() +
                ":#x;" + location.getX() +
                ":#y;" + location.getY() +
                ":#z;" + location.getZ() +
                ":#p;" + location.getPitch() +
                ":#yaw;" + location.getYaw();
    }

    public static boolean isInsideRadius(Location location, int radius) {
        return location.getBlockZ() >= -radius
                && location.getBlockZ() <= radius
                && location.getBlockX() >= -radius
                && location.getBlockX() <= radius;
    }

    public static boolean isInsideRadius(ExeUser<?> user, int radius) {
        return isInsideRadius(user.getLocation(), radius);
    }

    public static boolean isOutsideRadius(Location location, int radius) {
        return !isInsideRadius(location, radius);
    }

    public static boolean isOutsideRadius(ExeUser<?> user, int radius) {
        return !isInsideRadius(user, radius);
    }

    public static Block getHighestBlockNonAir(World world, int x, int z) { // implement loading on startup in UHC
        Block block = world.getHighestBlockAt(x, z);

        while (block.getY() > 0 && block.getType() == Material.AIR) {
            block = block.getRelative(BlockFace.DOWN);
        }
        return block;
    }

    public static Location center(Location location) {
        return location.getBlock().getLocation().add(
            (location.getBlock().getLocation().getX() < 0.0) ? -0.5 : 0.5,
            1.0,
            (location.getBlock().getLocation().getZ() < 0.0) ? -0.5 : 0.5);
    }
}
