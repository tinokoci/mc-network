package net.exemine.uhc.config.impl;

import net.exemine.api.util.string.CC;
import net.exemine.uhc.config.ConfigListener;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class HorsesListener extends ConfigListener {

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (entity.getType() == EntityType.HORSE) {
            event.setCancelled(true);
            player.updateInventory();
            player.sendMessage(CC.RED + "You cannot ride " + CC.BOLD + "Horses" + CC.RED + " because they are disabled.");
        }
    }
}
