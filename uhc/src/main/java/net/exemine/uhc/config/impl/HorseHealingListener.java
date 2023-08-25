package net.exemine.uhc.config.impl;

import net.exemine.uhc.config.ConfigListener;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class HorseHealingListener extends ConfigListener {

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (event.getEntityType() == EntityType.HORSE) {
            event.setCancelled(true);
        }
    }
}
