package net.exemine.uhc.scenario.loot;

import lombok.RequiredArgsConstructor;
import net.exemine.uhc.UHC;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.event.HeadPostSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

@RequiredArgsConstructor
public class LootChestListener implements Listener {

    private final UHC plugin;

    @EventHandler
    public void onHeadPostSpawn(HeadPostSpawnEvent event) {
        event.setCancelled(true); // always cancel this hehe
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        UHCUser user = plugin.getUserService().retrieve(event.getEntity().getUniqueId()); // retrieve because of combat logger
        if (!user.isPlaying()) return;

        plugin.getServer().getPluginManager().callEvent(
                new LootChestSpawnEvent(user, new LootChest(plugin, user, event.getDrops()).spawn()));
    }
}
