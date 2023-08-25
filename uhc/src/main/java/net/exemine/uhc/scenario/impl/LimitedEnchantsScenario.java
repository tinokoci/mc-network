package net.exemine.uhc.scenario.impl;

import net.exemine.api.util.string.CC;
import net.exemine.core.util.LocationUtil;
import net.exemine.uhc.game.event.GameStartEvent;
import net.exemine.uhc.scenario.ScenarioListener;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;

public class LimitedEnchantsScenario extends ScenarioListener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.ENCHANTMENT_TABLE) {
            event.getPlayer().sendMessage(CC.RED + "You can't break enchantment tables.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemCraft(CraftItemEvent event) {
        if (event.getRecipe().getResult().getType() == Material.ENCHANTMENT_TABLE) {
            event.getInventory().getViewers().forEach(humanEntity -> humanEntity.sendMessage(CC.RED + "You can't craft enchantment tables."));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onUHCGameStart(GameStartEvent event) {
        World world = plugin.getWorldService().getUhcWorld();
        LocationUtil.getHighestBlockNonAir(world, 0, 0).setType(Material.ENCHANTMENT_TABLE);

        int radius = plugin.getBorderService().getCurrentRadius().getValue() / 2;

        LocationUtil.getHighestBlockNonAir(world, radius, radius).setType(Material.ENCHANTMENT_TABLE);
        LocationUtil.getHighestBlockNonAir(world, radius, -radius).setType(Material.ENCHANTMENT_TABLE);
        LocationUtil.getHighestBlockNonAir(world, -radius, -radius).setType(Material.ENCHANTMENT_TABLE);
        LocationUtil.getHighestBlockNonAir(world, -radius, radius).setType(Material.ENCHANTMENT_TABLE);
    }
}
