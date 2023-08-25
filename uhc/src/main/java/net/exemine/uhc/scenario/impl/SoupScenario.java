package net.exemine.uhc.scenario.impl;

import net.exemine.uhc.scenario.ScenarioListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SoupScenario extends ScenarioListener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.MUSHROOM_SOUP) {
            if (player.getHealth() == 20) return;

            if (player.getHealth() <= 16) {
                player.setHealth(player.getHealth() + 4);
            } else {
                player.setHealth(20);
            }
            ItemStack itemInHand = player.getItemInHand();
            itemInHand.setType(Material.BOWL);
            itemInHand.setItemMeta(null);
            player.updateInventory();
        }
    }
}
