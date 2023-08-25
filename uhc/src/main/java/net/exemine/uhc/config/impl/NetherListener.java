package net.exemine.uhc.config.impl;

import net.exemine.api.util.string.CC;
import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPortalEvent;

public class NetherListener extends ConfigListener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.isCancelled() || plugin.getGameService().isStateOrLower(GameState.SCATTERING)) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(CC.RED + "You cannot go to the " + CC.BOLD + "Nether" + CC.RED + " because it is disabled.");
    }
}
