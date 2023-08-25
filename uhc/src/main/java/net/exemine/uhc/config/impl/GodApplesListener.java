package net.exemine.uhc.config.impl;

import net.exemine.api.util.string.CC;
import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.game.GameState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class GodApplesListener extends ConfigListener {

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (plugin.getGameService().isStateOrLower(GameState.SCATTERING)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();

        if (item.getType() == Material.GOLDEN_APPLE && item.getDurability() == 1) {
            event.setCancelled(true);
            player.updateInventory();
            player.sendMessage(CC.RED + "You cannot craft " + CC.BOLD + "God Apples" + CC.RED + " because they are disabled.");
        }
    }
}
