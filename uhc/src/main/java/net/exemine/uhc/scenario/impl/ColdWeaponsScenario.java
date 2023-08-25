package net.exemine.uhc.scenario.impl;

import net.exemine.uhc.scenario.ScenarioListener;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Map;

public class ColdWeaponsScenario extends ScenarioListener {

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        Map<Enchantment, Integer> toAdd = event.getEnchantsToAdd();

        if (toAdd.containsKey(Enchantment.FIRE_ASPECT)) {
            toAdd.remove(Enchantment.FIRE_ASPECT);
            toAdd.put(Enchantment.DAMAGE_ALL, 1);
        }
        if (toAdd.containsKey(Enchantment.ARROW_FIRE)) {
            toAdd.remove(Enchantment.ARROW_FIRE);
            toAdd.put(Enchantment.ARROW_DAMAGE, 2);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        InventoryType.SlotType slotType = event.getSlotType();

        if (inventory.getType().equals(InventoryType.ANVIL) && slotType.equals(InventoryType.SlotType.RESULT)) {
            ItemStack item = event.getCurrentItem();

            if (item.getEnchantments().containsKey(Enchantment.FIRE_ASPECT)) {
                item.removeEnchantment(Enchantment.FIRE_ASPECT);
            }
            if (item.getEnchantments().containsKey(Enchantment.ARROW_FIRE)) {
                item.removeEnchantment(Enchantment.ARROW_FIRE);
            }
            player.updateInventory();

            if (item.getType().equals(Material.ENCHANTED_BOOK)) {
                EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) item.getItemMeta();

                if (bookMeta.getStoredEnchants().containsKey(Enchantment.FIRE_ASPECT)) {
                    event.setCancelled(true);
                    item.removeEnchantment(Enchantment.FIRE_ASPECT);
                    player.closeInventory();
                }
                if (bookMeta.getStoredEnchants().containsKey(Enchantment.ARROW_FIRE)) {
                    event.setCancelled(true);
                    item.removeEnchantment(Enchantment.ARROW_FIRE);
                    player.closeInventory();
                }
            }
        }
    }
}
