package net.exemine.uhc.scenario.impl;

import net.exemine.uhc.scenario.ScenarioListener;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class VeinMinerScenario extends ScenarioListener {

    private static final EnumSet<BlockFace> FACES = EnumSet.of(
            BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
    );

    private final Set<Location> alreadyMined = new HashSet<>();

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.isSneaking()) {
            return;
        }
        if (alreadyMined.contains(event.getBlock().getLocation())) {
            return;
        }
        Block block = event.getBlock();

        if (block.getType().name().endsWith("_ORE")) {
            doTheThing(block.getType(), block, player);
        }
    }

    private void doTheThing(Material initType, Block block, Player player) {
        if (alreadyMined.contains(block.getLocation())) {
            return;
        }
        alreadyMined.add(block.getLocation());
        BlockBreakEvent event = new BlockBreakEvent(block, player);
        Bukkit.getPluginManager().callEvent(event);

        for (BlockFace blockFace : FACES) {
            Block relative = block.getRelative(blockFace);
            if (relative.getType() == initType || (relative
                    .getType() == Material.GLOWING_REDSTONE_ORE && initType == Material.REDSTONE_ORE) || (relative
                    .getType() == Material.REDSTONE_ORE && initType == Material.GLOWING_REDSTONE_ORE)) {
                doTheThing(initType, block.getRelative(blockFace), player);
            }
        }
    }
}
