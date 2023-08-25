package net.exemine.uhc.scenario.impl;

import net.exemine.uhc.scenario.ScenarioListener;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class FlowerPowerScenario extends ScenarioListener {

    private final Set<Material> disallowedMaterials = EnumSet.of(
            Material.BEDROCK, Material.TNT, Material.ENDER_PORTAL_FRAME, Material.PORTAL,
            Material.ENDER_PORTAL, Material.SKULL_ITEM, Material.EGG, Material.DRAGON_EGG,
            Material.MONSTER_EGG, Material.MONSTER_EGGS, Material.GOLDEN_APPLE, Material.ENDER_CHEST,
            Material.MOB_SPAWNER, Material.LAVA, Material.LAVA_BUCKET, Material.STATIONARY_LAVA,
            Material.WATER, Material.WATER_BUCKET, Material.STATIONARY_WATER, Material.EXPLOSIVE_MINECART
    );
    private final Set<Material> flowers = EnumSet.of(Material.RED_ROSE, Material.YELLOW_FLOWER, Material.DOUBLE_PLANT);
    private final List<Integer> allowedDoublePlantIDs = List.of(0, 1, 4, 5);

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block == null) return;

        Material type = block.getType();
        if (type == null) return;

        if ((type == Material.DOUBLE_PLANT && allowedDoublePlantIDs.stream().noneMatch(id -> id == block.getData()))
                || !flowers.contains(type)) return;

        block.getWorld().dropItemNaturally(block.getLocation(), getRandomItem());
        block.setType(Material.AIR);
        event.setCancelled(true);
    }

    private ItemStack getRandomItem() {
        Material material = Material.values()[ThreadLocalRandom.current().nextInt(Material.values().length)];

        if (disallowedMaterials.contains(material)) {
            return getRandomItem();
        }
        int amountToDrop = material.isSolid() ? getBetween(16, 32)
                : material.getMaxStackSize() > 1 ? getBetween(1, 5)
                : 1;
        return new ItemStack(material, amountToDrop);
    }

    private int getBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
