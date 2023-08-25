package net.exemine.uhc.scenario.impl;

import net.exemine.uhc.scenario.ScenarioListener;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @see net.exemine.uhc.scenario.BlockBreakListener for block break handling
 */
public class CutCleanScenario extends ScenarioListener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        List<ItemStack> drops = event.getDrops();

        switch (event.getEntityType()) {
            case COW:
            case MUSHROOM_COW:
                drops.clear();
                drops.add(new ItemStack(Material.LEATHER, 1));
                drops.add(new ItemStack(Material.COOKED_BEEF, 2));
                event.setDroppedExp((int) (Math.random() * 2 + 1));
                break;
            case PIG:
                drops.clear();
                drops.add(new ItemStack(Material.GRILLED_PORK, 1));
                event.setDroppedExp((int) (Math.random() * 2 + 1));
                break;
            case CHICKEN:
                drops.clear();
                drops.add(new ItemStack(Material.COOKED_CHICKEN, 2));
                drops.add(new ItemStack(Material.FEATHER, 1));
                event.setDroppedExp((int) (Math.random() * 2 + 1));
                break;
            case HORSE:
                drops.clear();
                drops.add(new ItemStack(Material.LEATHER, 1));
                event.setDroppedExp((int) (Math.random() * 2 + 1));
                break;
            case SHEEP:
                drops.clear();
                drops.add(new ItemStack(Material.WOOL));
                event.setDroppedExp((int) (Math.random() * 2 + 1));
                break;
        }
    }
}
