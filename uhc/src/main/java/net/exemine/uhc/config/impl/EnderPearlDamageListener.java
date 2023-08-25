package net.exemine.uhc.config.impl;

import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.game.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EnderPearlDamageListener extends ConfigListener {

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (plugin.getGameService().isStateOrLower(GameState.SCATTERING)) return;

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            player.setInvulnerableTicks(1);
            player.teleport(event.getTo());
        }
    }
}
