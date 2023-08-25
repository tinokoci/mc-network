package net.exemine.uhc.scenario.impl;

import net.exemine.api.util.string.CC;
import net.exemine.uhc.scenario.ScenarioListener;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BloodEnchantsScenario extends ScenarioListener {

    private final List<UUID> deathMessage = new ArrayList<>();

    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();
        int newLevel = event.getNewLevel();
        int oldLevel = event.getOldLevel();

        if (newLevel < oldLevel) {
            deathMessage.add(player.getUniqueId());
            player.damage((double) oldLevel - newLevel);
            deathMessage.remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        UHCUser user = plugin.getUserService().retrieve(event.getEntity()); // using retrieve because of combat logger

        if (deathMessage.contains(user.getUniqueId())) {
            user.setCustomDeathMessage("<victim> " + CC.GRAY + "enchanted to their own death.");
        }
    }
}
