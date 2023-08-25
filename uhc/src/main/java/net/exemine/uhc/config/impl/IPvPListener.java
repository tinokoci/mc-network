package net.exemine.uhc.config.impl;

import net.exemine.api.util.string.CC;
import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.game.GameState;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class IPvPListener extends ConfigListener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (plugin.getGameService().isStateOrLower(GameState.SCATTERING)
                || plugin.getGameService().isPvP())
            return;

        Entity entity = event.getEntity();
        EntityDamageEvent.DamageCause cause = event.getCause();

        if (entity instanceof Player && cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (plugin.getGameService().isStateOrLower(GameState.SCATTERING)
                || plugin.getGameService().isPvP())
            return;

        Player player = event.getPlayer();
        if (event.getBucket() == Material.LAVA_BUCKET && player.getNearbyEntities(5, 5, 5)
                .stream()
                .anyMatch(entity -> entity instanceof Player)) {
            event.setCancelled(true);
            player.sendMessage(CC.RED + "You cannot place lava here because " + CC.BOLD + "iPvP" + CC.RED + " is disabled.");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (plugin.getGameService().isStateOrLower(GameState.SCATTERING)
                || plugin.getGameService().isPvP()
                || event.getAction() != Action.RIGHT_CLICK_BLOCK
                || item == null
                || item.getType() != Material.FLINT_AND_STEEL)
            return;

        Player player = event.getPlayer();

        if (player.getNearbyEntities(5, 5, 5).stream().anyMatch(entity -> entity instanceof Player)) {
            event.setCancelled(true);
            player.sendMessage(CC.RED + "You cannot place lava fire because " + CC.BOLD + "iPvP" + CC.RED + " is disabled.");
        }
    }
}
