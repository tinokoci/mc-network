package net.exemine.core.util;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;

public class BlockUtil {

    public static boolean ensureAccess(Chest chest) {
        return chest != null && chest.getBlockInventory() != null;
    }

    public static Chest chestFromBlock(Block block) {
        Chest chest;
        try {
            chest = (Chest) block.getState();
        } catch (ClassCastException e) {
            chest = null;
        }
        return chest;
    }
}
