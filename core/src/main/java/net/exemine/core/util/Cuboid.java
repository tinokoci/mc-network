package net.exemine.core.util;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.bukkit.Location;

@Getter
public class Cuboid {

    public static Cuboid of(String worldName, int radius) {
        return new Cuboid(worldName, -radius, 0, -radius, radius, 256, radius);
    }

    private final String worldName;
    private final int minX, maxX, minY, maxY, minZ, maxZ;

    public Cuboid(String worldName, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.worldName = worldName;
        
        minX = Math.min(x1, x2);
        maxX = Math.max(x1, x2);
        minY = Math.min(y1, y2);
        maxY = Math.max(y1, y2);
        minZ = Math.min(z1, z2);
        maxZ = Math.max(z1, z2);
    }

    public List<Location> edges(Location loc) {
        List<Location> closestEdges = new ArrayList<>();
        int closestX = closestX(loc);
        int closestZ = closestZ(loc);
        for (int x = Math.max(this.minX, loc.getBlockX() - 5); x <= Math.min(this.maxX, loc.getBlockX() + 5); x++)
            closestEdges.add(new Location(loc.getWorld(), x, loc.getBlockY(), closestZ));
        for (int z = Math.max(this.minZ, loc.getBlockZ() - 5); z <= Math.min(this.maxZ, loc.getBlockZ() + 5); z++)
            closestEdges.add(new Location(loc.getWorld(), closestX, loc.getBlockY(), z));
        return closestEdges;
    }

    private int closestX(Location loc) {
        return (Math.abs(loc.getBlockX() - this.minX) < Math.abs(loc.getBlockX() - this.maxX)) ? this.minX : this.maxX;
    }

    private int closestZ(Location loc) {
        return (Math.abs(loc.getBlockZ() - this.minZ) < Math.abs(loc.getBlockZ() - this.maxZ)) ? this.minZ : this.maxZ;
    }

    public boolean contains(Location location) {
        return location.getBlockX() > minX && location.getBlockX() < maxX &&
                location.getBlockY() > minY && location.getBlockY() < maxY &&
                location.getBlockZ() > minZ && location.getBlockZ() < maxZ;
    }
}
