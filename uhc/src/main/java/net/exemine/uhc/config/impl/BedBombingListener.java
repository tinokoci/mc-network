package net.exemine.uhc.config.impl;

import net.exemine.api.util.string.CC;
import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BedBombingListener extends ConfigListener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (plugin.getGameService().isStateOrLower(GameState.SCATTERING)) return;

        UHCUser user = plugin.getUserService().get(event.getPlayer());
        ItemStack item = user.getItemInHand();

        if (user.inWorld(plugin.getWorldService().getNetherWorld())
                && (item.getType() == Material.BED) || (item.getType() == Material.BED_BLOCK)) {
            event.setCancelled(true);
            user.sendMessage(CC.RED + "You cannot explode a bed because " + CC.BOLD + "Bed Bombing" + CC.RED + " is disabled.");
        }
    }
}
