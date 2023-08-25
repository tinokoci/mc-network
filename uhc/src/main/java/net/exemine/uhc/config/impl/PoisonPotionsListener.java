package net.exemine.uhc.config.impl;

import net.exemine.api.util.string.CC;
import net.exemine.uhc.config.ConfigListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class PoisonPotionsListener extends ConfigListener {

    @EventHandler
    public void onPotionSplashEvent(PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();

        if (potion.getEffects().stream().anyMatch(effect -> effect.getType() == PotionEffectType.POISON)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        if (item.getType() != Material.POTION) return;
        Potion potion = Potion.fromItemStack(player.getItemInHand());

        if (potion.getType() == PotionType.POISON) {
            ItemStack air = new ItemStack(Material.AIR);

            event.setCancelled(true);
            event.setItem(air);
            player.setItemInHand(air);
            player.updateInventory();
            player.sendMessage(CC.RED + "You cannot use " + CC.BOLD + "Poison" + CC.RED + " potions because they are disabled.");
        }
    }
}
