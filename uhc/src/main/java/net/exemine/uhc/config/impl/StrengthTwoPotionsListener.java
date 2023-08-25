package net.exemine.uhc.config.impl;

import net.exemine.api.util.string.CC;
import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.config.option.ToggleOption;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

public class StrengthTwoPotionsListener extends ConfigListener {

    @EventHandler
    public void onPotionSplashEvent(PotionSplashEvent event) {
        Potion potion = Potion.fromDamage(event.getPotion().getItem().getDurability() & 0x3F);

        if (potion.getType() == PotionType.STRENGTH) {
            if (ToggleOption.STRENGTH_POTIONS_I.isEnabled() && potion.getLevel() > 1) {
                event.getAffectedEntities().forEach(entity -> {
                    potion.setLevel(1);
                    potion.apply(entity);
                });
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        if (item.getType() != Material.POTION) return;
        Potion potion = Potion.fromItemStack(player.getItemInHand());

        if (potion.getType() == PotionType.STRENGTH && potion.getLevel() > 1) {
            ItemStack air = new ItemStack(Material.AIR);

            event.setCancelled(true);
            event.setItem(air);
            player.setItemInHand(air);
            player.updateInventory();
            player.sendMessage(CC.RED + "You cannot use " + CC.BOLD + "Strength II" + CC.RED + " potions because they are disabled.");
        }
    }
}
