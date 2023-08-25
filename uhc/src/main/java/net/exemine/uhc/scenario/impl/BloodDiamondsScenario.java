package net.exemine.uhc.scenario.impl;

import net.exemine.api.util.string.CC;
import net.exemine.uhc.scenario.ScenarioListener;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BloodDiamondsScenario extends ScenarioListener {

    private final List<UUID> deathMessage = new ArrayList<>();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (event.getBlock().getType() == Material.DIAMOND_ORE) {
            deathMessage.add(player.getUniqueId());
            player.damage(1.0);
            deathMessage.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        UHCUser user = plugin.getUserService().retrieve(event.getEntity()); // using retrieve because of combat logger

        if (deathMessage.contains(user.getUniqueId())) {
            user.setCustomDeathMessage("<victim> " + CC.GRAY + "mined diamonds to their own death.");
        }
    }
}
