package net.exemine.uhc.scenario.impl;

import net.exemine.core.util.item.ItemBuilder;
import net.exemine.uhc.config.option.ToggleOption;
import net.exemine.uhc.scenario.ScenarioListener;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;

/**
 * @see net.exemine.uhc.scenario.BlockBreakListener for block break handling
 */
public class GoldlessScenario extends ScenarioListener {

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Iterator<Block> iterator = event.blockList().iterator();

        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (block.getType() == Material.GOLD_ORE) {
                block.setType(Material.AIR);
                iterator.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDeath(PlayerDeathEvent event) {
        UHCUser user = plugin.getUserService().retrieve(event.getEntity()); // using retrieve because of combat logger
        if (!user.isPlaying()) return;

        List<ItemStack> drops = event.getDrops();
        drops.add(new ItemStack(Material.GOLD_INGOT, 8));

        if (ToggleOption.GOLDEN_HEADS.isEnabled()) {
            drops.add(ItemBuilder.getGoldenHead());
        }
    }
}
