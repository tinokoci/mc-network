package net.exemine.uhc.scenario;

import lombok.RequiredArgsConstructor;
import net.exemine.api.util.MathUtil;
import net.exemine.api.util.MultiValueMap;
import net.exemine.api.util.string.CC;
import net.exemine.core.util.MessageUtil;
import net.exemine.core.util.item.ItemUtil;
import net.exemine.core.util.spigot.Clickable;
import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.logger.meta.MetaService;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import net.exemine.uhc.user.info.GameInfo;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

// Class for most block break scenarios, so we don't have to deal with event priorities etc.
// Managing different scenario combinations is hard without everything being in the same place
@RequiredArgsConstructor
public class BlockBreakListener implements Listener {

    private final MetaService metaService;
    private final UHCUserService userService;

    private final Map<UUID, MultiValueMap<Material, Long>> blockMineLogs = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        UHCUser user = userService.get(event.getPlayer());
        if (event.isCancelled() || user.getGameMode() == GameMode.CREATIVE) return;

        Block block = event.getBlock();
        Location location = block.getLocation();
        World world = block.getWorld();
        Material blockType = block.getType();
        Material itemInHand = user.getItemInHand().getType();
        GameInfo gameInfo = user.getGameInfo();

        switch (blockType) {
            case MOB_SPAWNER:
                userService.getModAndHostUsers()
                        .stream()
                        .filter(gameMod -> gameMod.getStaffData().isXrayAlerts())
                        .forEach(gameMod -> new Clickable()
                                .add(CC.RED + "[Warning] " + user.getColoredDisplayName() + CC.GRAY + " just mined a " + CC.GOLD + "Mob Spawner" + CC.GRAY + ". ")
                                .add(CC.GREEN + "[Teleport]", CC.GREEN + "Click to teleport.", "/tp " + user.getDisplayName())
                                .send(gameMod));
                break;
            case DIAMOND_ORE:
                if (Stream.of(Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE)
                        .noneMatch(item -> item == itemInHand)) return;

                if (Scenario.BROADCASTER.isEnabled() && MathUtil.tryChance(20)) {
                    MessageUtil.send(CC.BOLD_GOLD + "BROADCASTER! " + user.getColoredDisplayName() + CC.GRAY + " just mined a diamond ore at " + CC.PINK + '(' + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ')' + CC.GRAY + '.');
                }
                block.setType(Material.AIR);
                if (Scenario.DIAMOND_LESS.isDisabled()) {
                    mineOre(user, location, Material.DIAMOND, Material.DIAMOND);
                }
                dropExperience(user, location, blockType);
                handleLog(user, "diamond", CC.AQUA, Material.DIAMOND_ORE, 16);
                gameInfo.getMinedDiamonds().increment();
                break;
            case EMERALD_ORE:
                if (Stream.of(Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE)
                        .noneMatch(item -> item == itemInHand)) return;

                block.setType(Material.AIR);
                mineOre(user, location, Material.EMERALD, Material.EMERALD);
                dropExperience(user, location, blockType);
                break;
            case GOLD_ORE:
                if (Stream.of(Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE)
                        .noneMatch(item -> item == itemInHand)) return;

                block.setType(Material.AIR);
                if (Scenario.GOLD_LESS.isDisabled()) {
                    mineOre(user, location, blockType, Material.GOLD_INGOT);
                }
                dropExperience(user, location, blockType);
                handleLog(user, "gold", CC.GOLD, Material.GOLD_ORE, 32);
                gameInfo.getMinedGold().increment();
                break;
            case IRON_ORE:
                if (Stream.of(Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE, Material.STONE_PICKAXE)
                        .noneMatch(item -> item == itemInHand)) return;

                block.setType(Material.AIR);
                mineOre(user, location, blockType, Material.IRON_INGOT);
                dropExperience(user, location, blockType);
                gameInfo.getMinedIron().increment();
                break;
            case COAL_ORE:
                if (!itemInHand.name().endsWith("_PICKAXE")) return;

                block.setType(Material.AIR);
                mineOre(user, location, Material.COAL, Material.COAL);
                dropExperience(user, location, blockType);
                gameInfo.getMinedCoal().increment();
                break;
            case LAPIS_ORE:
                if (Stream.of(Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE, Material.STONE_PICKAXE)
                        .noneMatch(item -> item == itemInHand)) return;

                block.setType(Material.AIR);
                mineOre(user, location, Material.LAPIS_BLOCK, Material.LAPIS_BLOCK, MathUtil.getIntBetween(3, 4));
                dropExperience(user, location, blockType);
                gameInfo.getMinedLapis().increment();
                break;
            case REDSTONE_ORE:
            case GLOWING_REDSTONE_ORE:
                if (Stream.of(Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE)
                        .noneMatch(item -> item == itemInHand)) return;

                block.setType(Material.AIR);
                mineOre(user, location, Material.REDSTONE, Material.REDSTONE, MathUtil.getIntBetween(2, 3));
                dropExperience(user, location, blockType);
                gameInfo.getMinedRedstone().increment();
                break;
            case QUARTZ_ORE:
                if (Stream.of(Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE)
                        .noneMatch(item -> item == itemInHand)) return;

                block.setType(Material.AIR);
                mineOre(user, location, Material.QUARTZ, Material.QUARTZ);
                dropExperience(user, location, blockType);
                gameInfo.getMinedQuartz().increment();
                break;
            case GRAVEL:
                block.setType(Material.AIR);
                boolean dropRate = MathUtil.tryChance(Scenario.VANILLA_PLUS.isEnabled() ? 70 : 10);
                mineOre(user, location, dropRate ? Material.FLINT : blockType, Material.FLINT);
                break;
            case LEAVES:
            case LEAVES_2:
                if (MathUtil.tryChance(NumberOption.APPLE_RATE.getValue())) {
                    world.dropItemNaturally(block.getLocation(), new ItemStack(Material.APPLE));
                }
                if (Scenario.LUCKY_LEAVES.isEnabled() && MathUtil.tryChance(1)) {
                    world.dropItemNaturally(block.getLocation(), new ItemStack(Material.GOLDEN_APPLE));
                }
        }
    }

    private void mineOre(UHCUser user, Location location, Material dropMaterial, Material smeltedMaterial, int amount) {
        // Replace ores with iron if bare bones is enabled
        Material finalSmeltedMaterial = smeltedMaterial;
        if (Scenario.BARE_BONES.isEnabled() &&
                Stream.of(
                        Material.DIAMOND,
                        Material.EMERALD,
                        Material.LAPIS_BLOCK,
                        Material.REDSTONE,
                        Material.COAL,
                        Material.EMERALD_ORE
                ).anyMatch(material -> finalSmeltedMaterial == material)) {
            dropMaterial = Material.IRON_ORE;
            smeltedMaterial = Material.IRON_INGOT;
            amount = 1;
        }
        ItemStack itemInHand = user.getItemInHand();

        // Account for fortune levels
        if (itemInHand.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
            int fortuneLevel = itemInHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
            int addition = MathUtil.getIntBetween(1, fortuneLevel);
            amount += addition;
        }
        // Increase amount of drops if Triple Ores is enabled
        if (Scenario.TRIPLE_ORES.isEnabled()) {
            amount *= 3;
        }
        // Increase amount of drops if Double Ores is enabled
        if (Scenario.DOUBLE_ORES.isEnabled()) {
            amount *= 2;
        }
        // Create ItemStack with either smelted drop or normal drop depending if CutClean is enabled
        List<ItemStack> itemsToDrop = new ArrayList<>();

        if (smeltedMaterial == Material.LAPIS_BLOCK) { // there's no lazuli material so manually handle it here
            Dye lapis = new Dye();
            lapis.setColor(DyeColor.BLUE);
            itemsToDrop.add(lapis.toItemStack(amount));
        } else {
            Material material = Scenario.CUT_CLEAN.isEnabled() ? smeltedMaterial : dropMaterial;
            itemsToDrop.add(new ItemStack(material, amount));
        }

        // Replace item drops if ore frenzy is enabled
        if (Scenario.ORE_FRENZY.isEnabled()) {
            switch (dropMaterial) {
                case LAPIS_BLOCK:
                    itemsToDrop.clear();
                    itemsToDrop.add(new ItemStack(Material.POTION, amount, (short) 16389));
                    break;
                case EMERALD:
                    itemsToDrop.clear();
                    itemsToDrop.add(new ItemStack(Material.ARROW, 32 * amount));
                    break;
                case REDSTONE:
                    itemsToDrop.clear();
                    itemsToDrop.add(new ItemStack(Material.BOOK, amount));
                    break;
                case DIAMOND:
                    itemsToDrop.clear();
                    itemsToDrop.add(new ItemStack(Material.DIAMOND, amount));
                    itemsToDrop.add(new ItemStack(Material.EXP_BOTTLE, 4 * amount));
                    break;
                case QUARTZ:
                    itemsToDrop.clear();
                    itemsToDrop.add(new ItemStack(Material.TNT, amount));
            }
        }
        // Put items directly in inventory if InstantInv is enabled
        if (Scenario.INSTANT_INV.isEnabled()) {
            Map<Integer, ItemStack> notGivenItemMap = user.getInventory().addItem(itemsToDrop.toArray(new ItemStack[0]));
            if (notGivenItemMap.isEmpty()) return;
            notGivenItemMap.values().forEach(item -> ItemUtil.dropItem(item, location.getBlock(), user.getPlayer()));
        } else {
            itemsToDrop.forEach(itemToDrop -> ItemUtil.dropItem(itemToDrop, location.getBlock(), user.getPlayer()));
        }
    }

    private void mineOre(UHCUser user, Location location, Material dropMaterial, Material smeltedMaterial) {
        mineOre(user, location, dropMaterial, smeltedMaterial, 1);
    }

    private void dropExperience(UHCUser user, Location location, Material minedMaterial) {
        int experience = metaService.getXpFromOre(minedMaterial);
        if (experience == 0) return;

        // If InstantInv is enabled, add experience directly to the user
        /*if (Scenario.INSTANT_INV.isEnabled()) {
            user.giveExp(experience); // TODO: gives less than the orb below, debug later
            return;
        }*/
        ExperienceOrb experienceOrb = (ExperienceOrb) location.getWorld().spawnEntity(location, EntityType.EXPERIENCE_ORB);
        experienceOrb.setExperience(Scenario.VEIN_MINER.isEnabled() && !user.isSneaking() ? experience * 2 : experience);
    }

    private void handleLog(UHCUser user, String name, String color, Material material, int amount) {
        blockMineLogs.putIfAbsent(user.getUniqueId(), new MultiValueMap<>());

        MultiValueMap<Material, Long> userBlockMineLogs = blockMineLogs.get(user.getUniqueId());
        List<Long> times = userBlockMineLogs.get(material);

        userBlockMineLogs.put(material, System.currentTimeMillis());
        times.removeIf(time -> time + 600000L < System.currentTimeMillis());

        if (times.size() >= amount) {
            userService.getModAndHostUsers()
                    .stream()
                    .filter(gameMod -> gameMod.getStaffData().isXrayAlerts())
                    .forEach(gameMod -> new Clickable()
                            .add(CC.RED + "[Warning] " + user.getColoredDisplayName() + CC.GRAY + " is mining " + color + name + ' ' + color + "ores " + CC.GRAY + "too quickly. ")
                            .add(CC.GREEN + "[Teleport]", CC.GREEN + "Click to teleport.", "/tp " + user.getDisplayName())
                            .send(gameMod));
            times.clear();
        }

    }
}
