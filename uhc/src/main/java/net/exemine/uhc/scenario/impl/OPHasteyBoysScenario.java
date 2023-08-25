package net.exemine.uhc.scenario.impl;

import net.exemine.uhc.scenario.ScenarioListener;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Stream;

public class OPHasteyBoysScenario extends ScenarioListener {

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        Material resultType = event.getRecipe().getResult().getType();
        if (Stream.of("_PICKAXE", "_AXE", "_SPADE").noneMatch(suffix -> resultType.name().endsWith(suffix))) return;

        ItemStack item = new ItemStack(resultType);
        item.addEnchantment(Enchantment.DIG_SPEED, 5);
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 5);

        event.getInventory().setResult(item);
    }
}
