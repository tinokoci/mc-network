package net.exemine.uhc.config.impl;

import net.exemine.api.util.Executor;
import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.game.GameState;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffectType;

public class AbsorptionListener extends ConfigListener {

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (plugin.getGameService().isStateOrLower(GameState.SCATTERING)) return;

        if (event.getItem().getType() == Material.GOLDEN_APPLE) {
            Executor.schedule(() -> event.getPlayer().removePotionEffect(PotionEffectType.ABSORPTION)).runSyncLater(100L);
        }
    }
}
