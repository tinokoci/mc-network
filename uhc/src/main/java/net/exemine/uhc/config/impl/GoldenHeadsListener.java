package net.exemine.uhc.config.impl;

import net.exemine.core.util.item.ItemBuilder;
import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.event.HeadPostSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

public class GoldenHeadsListener extends ConfigListener {

    @Override
    public void onEnable() {
        ItemBuilder.addGoldenHeadRecipe();
    }

    @Override
    public void onDisable() {
        ItemBuilder.removeGoldenHeadRecipe();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player entity = event.getEntity();
        UHCUser user = plugin.getUserService().retrieve(entity.getUniqueId()); // retrieve because of combat logger

        HeadPostSpawnEvent headPostSpawnEvent = new HeadPostSpawnEvent(user);
        Bukkit.getPluginManager().callEvent(headPostSpawnEvent);

        if (user.isPlaying() && !headPostSpawnEvent.isCancelled()) {
            user.spawnHead(entity.getLocation());
        }
    }
}
