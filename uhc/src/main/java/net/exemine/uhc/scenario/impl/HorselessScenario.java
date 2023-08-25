package net.exemine.uhc.scenario.impl;

import net.exemine.api.util.string.CC;
import net.exemine.uhc.scenario.ScenarioListener;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class HorselessScenario extends ScenarioListener {

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity instanceof Horse) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(CC.RED + "You cannot ride horses while the " + CC.BOLD + "Horseless" + CC.RED + " scenario is active.");
            entity.remove();
        }
    }
}
