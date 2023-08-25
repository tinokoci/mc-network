package net.exemine.uhc.scenario.impl;

import net.exemine.core.util.item.ItemBuilder;
import net.exemine.uhc.scenario.ScenarioListener;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

public class GoldenRetrieverScenario extends ScenarioListener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        UHCUser user = plugin.getUserService().retrieve(event.getEntity()); // using retrieve because of combat logger
        if (!user.isPlaying()) return;

        event.getDrops().add(ItemBuilder.getGoldenHead());
    }
}
