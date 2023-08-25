package net.exemine.uhc.border.task;

import lombok.Getter;
import net.exemine.uhc.UHC;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class BorderPlaceTask extends BukkitRunnable {

    private final int radius;
    private final World world;

    private int counter;
    private boolean phase1;
    private boolean phase2;
    private boolean phase3;

    @Getter
    private static final List<Material> blockedWallBlocks = List.of(
            Material.LOG, Material.LOG_2, Material.LEAVES, Material.LEAVES_2,
            Material.AIR, Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA,
            Material.HUGE_MUSHROOM_1, Material.HUGE_MUSHROOM_2, Material.DOUBLE_PLANT, Material.LONG_GRASS,
            Material.VINE, Material.YELLOW_FLOWER, Material.RED_ROSE, Material.CACTUS, Material.DEAD_BUSH,
            Material.SUGAR_CANE_BLOCK, Material.ICE, Material.SNOW
    );

    public BorderPlaceTask(UHC plugin, int radius) {
        this.radius = radius;
        this.counter = -radius;
        this.world = plugin.getWorldService().getUhcWorld();
        runTaskTimer(plugin, 0, 5L);
    }

    @Override
    public void run() {
        if (!phase1) {
            int maxCounter = counter + 500;
            int x = -radius;

            for (int z = counter; z <= radius && counter <= maxCounter; z++, counter++) {
                figureOutBlockToMakeBedrock(world, x, z);
            }
            if (counter >= radius) {
                counter = -radius;
                phase1 = true;
            }
            return;
        }
        if (!phase2) {
            int maxCounter = counter + 500;

            for (int z = counter; z <= radius && counter <= maxCounter; z++, counter++) {
                figureOutBlockToMakeBedrock(world, radius, z);
            }
            if (counter >= radius) {
                counter = -radius;
                phase2 = true;
            }
            return;
        }
        if (!phase3) {
            int maxCounter = counter + 500;
            int z = -radius;

            for (int x = counter; x <= radius && counter <= maxCounter; x++, counter++) {
                if (x == radius || x == -radius) {
                    continue;
                }
                figureOutBlockToMakeBedrock(world, x, z);
            }
            if (counter >= radius) {
                counter = -radius;
                phase3 = true;
            }
            return;
        }
        int maxCounter = counter + 500;

        for (int x = counter; x <= radius && counter <= maxCounter; x++, counter++) {
            if (x == radius || x == -radius) {
                continue;
            }
            figureOutBlockToMakeBedrock(world, x, radius);
        }
        if (counter >= radius) {
            cancel();
        }
    }

    private static void figureOutBlockToMakeBedrock(World world, int x, int z) {
        Block block = world.getHighestBlockAt(x, z);
        Block below = block.getRelative(BlockFace.DOWN);

        while (blockedWallBlocks.contains(below.getType()) && below.getY() > 1) {
            below = below.getRelative(BlockFace.DOWN);
        }
        below.getRelative(BlockFace.UP).setType(Material.BEDROCK);
    }
}
