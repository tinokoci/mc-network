package net.exemine.uhc.config.impl;

import net.exemine.api.util.string.CC;
import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPortalEvent;

public class NetherBeforePvPListener extends ConfigListener {

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerPortal(PlayerPortalEvent event) {
        GameService gameService = plugin.getGameService();
        if (event.isCancelled() || gameService.isStateOrLower(GameState.SCATTERING) || gameService.isPvP()) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(CC.RED + "You cannot go to the nether because " + CC.BOLD + "Nether Before PvP" + CC.RED + " is disabled.");
    }
}
