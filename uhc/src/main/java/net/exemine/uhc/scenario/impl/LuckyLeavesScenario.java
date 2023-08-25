package net.exemine.uhc.scenario.impl;

import net.exemine.api.util.MathUtil;
import net.exemine.uhc.scenario.ScenarioListener;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @see net.exemine.uhc.scenario.BlockBreakListener for block break handling
 */
public class LuckyLeavesScenario extends ScenarioListener {

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (MathUtil.tryChance(0.5f)) {
            Block block = event.getBlock();
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.GOLDEN_APPLE));
        }
    }
}
