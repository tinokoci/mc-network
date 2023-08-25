package net.exemine.uhc.scenario.impl;

import net.exemine.uhc.scenario.ScenarioListener;
import net.exemine.uhc.world.WorldService;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class FirelessScenario extends ScenarioListener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        EntityDamageEvent.DamageCause cause = event.getCause();

        WorldService worldService = plugin.getWorldService();
        if (worldService.isWorld(entity.getWorld(), worldService.getNetherWorld())) return;

        if ((cause == EntityDamageEvent.DamageCause.FIRE
                || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                || cause == EntityDamageEvent.DamageCause.LAVA)
                && entity instanceof Player) {
            event.setCancelled(true);
        }
    }
}
