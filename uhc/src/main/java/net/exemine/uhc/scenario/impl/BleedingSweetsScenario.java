package net.exemine.uhc.scenario.impl;

import net.exemine.uhc.scenario.ScenarioListener;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BleedingSweetsScenario extends ScenarioListener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        UHCUser user = plugin.getUserService().retrieve(event.getEntity()); // using retrieve because of combat logger
        if (!user.isPlaying()) return;

        event.getDrops().addAll(List.of(
                new ItemStack(Material.DIAMOND),
                new ItemStack(Material.GOLD_INGOT, 5),
                new ItemStack(Material.BOOK),
                new ItemStack(Material.STRING),
                new ItemStack(Material.ARROW, 16)
        ));
    }
}
