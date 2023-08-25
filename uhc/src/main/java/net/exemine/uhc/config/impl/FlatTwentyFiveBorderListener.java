package net.exemine.uhc.config.impl;

import com.boydti.fawe.bukkit.wrapper.AsyncWorld;
import com.boydti.fawe.util.TaskManager;
import net.exemine.api.util.Executor;
import net.exemine.core.util.LocationUtil;
import net.exemine.uhc.border.BorderRadius;
import net.exemine.uhc.border.event.BorderBuildEvent;
import net.exemine.uhc.border.event.BorderShrinkEvent;
import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.scenario.Scenario;
import net.exemine.uhc.world.WorldService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FlatTwentyFiveBorderListener extends ConfigListener {

    @EventHandler
    public void onBorderBuild(BorderBuildEvent event) {
        BorderRadius borderRadius = event.getBorderRadius();
        if (!borderRadius.isEqual(BorderRadius.RADIUS_25)) return;
        event.setCancelled(true); // we manually build border here
    }

    @EventHandler
    public void onBorderShrink(BorderShrinkEvent event) {
        if (event.isCancelled()) return;

        BorderRadius borderRadius = event.getBorderRadius();
        if (!borderRadius.isEqual(BorderRadius.RADIUS_25)) return;

        WorldService worldService = plugin.getWorldService();
        World world = worldService.getUhcWorld();

        int radius = borderRadius.getValue();
        int lowestY = 65;

        TaskManager.IMP.async(() -> {
            AsyncWorld asyncWorld = AsyncWorld.wrap(world);
            for (int x = -radius; x < radius + 1; x++) {
                for (int z = -radius; z < radius + 1; z++) {
                    for (int y = lowestY; y < 150; y++) {
                        asyncWorld.getBlockAt(x, y, z).setType(y == lowestY ? Material.GRASS : Material.AIR);
                    }
                }
            }
            List<Location> locations = new ArrayList<>();

            for (int y = lowestY + 1; y < lowestY + plugin.getBorderService().getBorderHeight() + 1; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location location = new Location(world, radius, y, z);
                    locations.add(location);
                }
                for (int z = -radius; z <= radius; z++) {
                    Location location = new Location(world, -radius, y, z);
                    locations.add(location);
                }
                for (int x = -radius; x <= radius; x++) {
                    Location location = new Location(world, x,  y, radius);
                    locations.add(location);
                }
                for (int x = -radius; x <= radius; x++) {
                    Location location = new Location(world, x,  y, -radius);
                    locations.add(location);
                }
            }
            locations.forEach(location -> asyncWorld.getBlockAt(location).setType(Material.BEDROCK));
            asyncWorld.commit();

            Executor.schedule(() -> {
                plugin.getUserService().getOnlineUsers().forEach(onlineUser -> {
                    Location location = onlineUser.getLocation();
                    onlineUser.setFallDistance(0.0f);
                    onlineUser.teleport(new Location(location.getWorld(), location.getX(), lowestY + 1, location.getZ(), location.getYaw(), location.getPitch()));
                    onlineUser.setFallDistance(0.0f);
                });
                world.getEntities().stream().filter(entity -> {
                    if (entity instanceof Item) {
                        Item item = (Item) entity;
                        ItemStack stack = item.getItemStack();

                        return stack != null
                                && Stream.of(Material.SEEDS, Material.YELLOW_FLOWER, Material.SAPLING, Material.RED_ROSE, Material.DOUBLE_PLANT)
                                .anyMatch(material -> stack.getType() == material);
                    }
                    return false;
                }).forEach(Entity::remove);

                if (Scenario.LIMITED_ENCHANTS.isEnabled()) {
                    LocationUtil.getHighestBlockNonAir(world, 0, 0).setType(Material.ENCHANTMENT_TABLE);
                }
            }).runSync();
        });
    }
}
