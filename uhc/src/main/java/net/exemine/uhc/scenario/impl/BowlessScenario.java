package net.exemine.uhc.scenario.impl;

import net.exemine.api.util.string.CC;
import net.exemine.uhc.scenario.ScenarioListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BowlessScenario extends ScenarioListener {

    @EventHandler
    public void onPlayerCraft(CraftItemEvent event) {
        if (event.getRecipe().getResult().getType() == Material.BOW) {
            event.setCancelled(true);
            ((Player) event.getWhoClicked()).sendMessage(CC.RED + "You cannot craft bows while the " + CC.BOLD + "Bowless" + CC.RED + " scenario is active.");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        Player player = event.getPlayer();

        if (player.getItemInHand().getType() == Material.BOW
                && (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR)) {
            event.setCancelled(true);
            player.setItemInHand(new ItemStack(Material.AIR));
            player.updateInventory();
            player.sendMessage(CC.RED + "You cannot use bows while the " + CC.BOLD + "Bowless" + CC.RED + " scenario is active.");
        }
    }
}
