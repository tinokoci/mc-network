package net.exemine.uhc.scenario.impl;

import net.exemine.uhc.scenario.ScenarioListener;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Map;

public class TimberScenario extends ScenarioListener {

    private static final EnumSet<BlockFace> FACES = EnumSet.of(
            BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    );

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

        if (event.getBlock().getType() == Material.LOG) {
            breakRecursive(event.getPlayer(), event.getBlock());
        } else if (event.getBlock().getType() == Material.LOG_2) {
            breakIterative(event.getPlayer(), event.getBlock());
        }
    }

    private void breakRecursive(Player player, Block log) {
        if (log.getType() == Material.LOG) {
            breakLog(player, log);

            for (BlockFace face : FACES) {
                breakRecursive(player, log.getRelative(face));
            }
        }
    }

    private void breakIterative(Player player, Block block) {
        breakLog(player, block);
        Location location = block.getLocation();
        int expander2D = 3;
        int expander3D = 8;
        for (int x = location.getBlockX() - expander2D; x < location.getBlockX() + expander2D; x++) {
            for (int y = location.getBlockY() - expander3D; y < location.getBlockY() + expander3D; y++) {
                for (int z = location.getBlockZ() - expander2D; z < location.getBlockZ() + expander2D; z++) {
                    Block toBreak = block.getWorld().getBlockAt(x, y, z);
                    if (toBreak.getType() == Material.LOG_2) {
                        breakLog(player, toBreak);
                    }
                }
            }
        }
    }

    private void breakLog(Player player, Block log) {
        Map<Integer, ItemStack> result = player.getInventory().addItem(log.getDrops().toArray(new ItemStack[0]));

        if (result.isEmpty()) {
            // Log could be added to the player's inventory, now emulate block break
            log.setType(Material.AIR);
        } else {
            // Log couldn't be added since the inventory was full, just break & drop the log
            log.breakNaturally();
        }
    }
}
