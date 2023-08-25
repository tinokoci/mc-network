package net.exemine.uhc.scenario.loot;

import lombok.Getter;
import net.exemine.api.util.Executor;
import net.exemine.core.util.BlockUtil;
import net.exemine.core.util.LocationUtil;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.uhc.UHC;
import net.exemine.uhc.border.BorderRadius;
import net.exemine.uhc.config.option.ToggleOption;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LootChest {

    public static final String LOCK_KEY = "protected-chest";

    private final UHC plugin;
    private final Location location;
    private final List<ItemStack> drops;

    @Getter
    private Chest leftChest, rightChest;

    public LootChest(UHC plugin, Player corpse, List<ItemStack> drops) {
        this.plugin = plugin;

        location = corpse.getLocation();
        this.drops = drops;
    }

    /*public LootChest(PluginAdapter<?> plugin, GameRelogger relogEntity) {
        this.plugin = plugin;

        location = relogEntity.getEntity().getEntity().getLocation();

        inventoryContents = relogEntity.getItems();
        armorContents = relogEntity.getArmor();
    }*/

    public LootChest spawn() {
        Block leftChestBlock, rightChestBlock;
        (leftChestBlock = location.getBlock()).setType(Material.CHEST);
        (rightChestBlock = leftChestBlock.getRelative(BlockFace.NORTH)).setType(Material.CHEST);

        leftChestBlock.getRelative(BlockFace.UP).setType(Material.AIR);
        rightChestBlock.getRelative(BlockFace.UP).setType(Material.AIR);

        leftChest = BlockUtil.chestFromBlock(leftChestBlock);
        rightChest = BlockUtil.chestFromBlock(rightChestBlock);

        drops.stream()
                .filter(itemStack -> itemStack != null &&
                        itemStack.getType() != Material.AIR)
                .forEach(itemStack -> leftChest.getInventory().addItem(itemStack));
        drops.clear();
        if (ToggleOption.GOLDEN_HEADS.isEnabled()) {
            leftChest.getInventory().addItem(ItemBuilder.getGoldenHead());
        }
        Executor.schedule(this::despawnWithoutDrops).runSyncLater(120_000L);
        return this;
    }

    public Collection<ItemStack> addItems(ItemStack... itemStacks) {
        return leftChest.getInventory().addItem(itemStacks).values();
    }

    public void lock(Set<UUID> excludedPlayers) {
        leftChest.setMetadata(LOCK_KEY, new FixedMetadataValue(plugin, excludedPlayers));
        rightChest.setMetadata(LOCK_KEY, new FixedMetadataValue(plugin, excludedPlayers));
    }

    public void unlock() {
        leftChest.removeMetadata(LOCK_KEY, plugin);
        rightChest.removeMetadata(LOCK_KEY, plugin);
    }

    public void despawnWithoutDrops() {
        List.of(leftChest, rightChest).forEach(chest -> {
            if (BlockUtil.ensureAccess(chest)) {
                chest.getBlockInventory().clear();
                chest.getLocation().getBlock().setType(Material.AIR);

                // Restore terrain
                Block blockBelow = chest.getLocation().getBlock().getRelative(BlockFace.DOWN);
                if (blockBelow.getType() == Material.DIRT) {
                    blockBelow.setType(Material.GRASS);
                }
            }
        });
    }

    public void explode() {
        despawnWithoutDrops();

        location.getWorld().spigot().strikeLightning(location, true);
        location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(),
                plugin.getBorderService().getCurrentRadius().isEqualOrLower(BorderRadius.RADIUS_100) ? 0 : 8,
                false, LocationUtil.isOutsideRadius(location, 100));
    }
}
