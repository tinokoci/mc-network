package net.exemine.uhc.scenario.impl;

import net.exemine.api.util.string.CC;
import net.exemine.uhc.scenario.ScenarioListener;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BarebonesScenario extends ScenarioListener {

    private final Set<Material> materials = new HashSet<Material>() {{
        add(Material.ANVIL);
        add(Material.ENCHANTMENT_TABLE);
        add(Material.GOLDEN_APPLE);
    }};

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        UHCUser user = plugin.getUserService().retrieve(event.getEntity()); // using retrieve because of combat logger
        if (!user.isPlaying()) return;

        event.getDrops().addAll(List.of(
                new ItemStack(Material.DIAMOND, 1),
                new ItemStack(Material.ARROW, 32),
                new ItemStack(Material.GOLDEN_APPLE, 1),
                new ItemStack(Material.STRING, 2)
        ));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.isCancelled()) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(CC.RED + "The nether is currently disabled due to the " + CC.BOLD + "Barebones" + CC.RED + " scenario being active.");
    }

    @EventHandler
    public void onItemCraft(CraftItemEvent event) {
        if (materials.contains(event.getRecipe().getResult().getType())) {
            event.getWhoClicked().sendMessage(CC.RED + "You cannot craft that due to the " + CC.BOLD + "Barebones" + CC.RED + " scenario being active.");
            event.setCancelled(true);
        }
    }
}
