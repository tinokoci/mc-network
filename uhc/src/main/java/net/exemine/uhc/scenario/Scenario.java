package net.exemine.uhc.scenario;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.exemine.api.model.ScenarioName;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.util.MessageUtil;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.uhc.UHC;
import net.exemine.uhc.scenario.impl.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum Scenario {
    CUT_CLEAN(ScenarioName.CUT_CLEAN, new CutCleanScenario(), Material.IRON_INGOT, "Ores and food are automatically cooked or smelted. You always get leather from cows and horses. Apple rate is increased to 3%. Flint rate is increased to 100%."),
    HASTEY_BOY(ScenarioName.HASTEY_BOYS, new HasteyBoysScenario(), Material.GOLD_PICKAXE, "Crafted tools are enchanted with Efficiency III and Unbreaking III."),
    TIMBER(ScenarioName.TIMBER, new TimberScenario(), Material.LOG, "When you break one log, it automatically breaks the whole tree."),
    NO_CLEAN(ScenarioName.NO_CLEAN, new NoCleanScenario(), Material.DIAMOND_SWORD, "You are invincible for 30 seconds when killing a player. You will lose your invincibility when attacking someone."),
    TIME_BOMB(ScenarioName.TIME_BOMB, new TimeBombScenario(), Material.TNT, "Loot chests explode after 30 seconds."),
    SAFE_LOOT(ScenarioName.SAFE_LOOT, new SafeLootScenario(), Material.CHEST, "Loot chests are protected for 30 seconds."),
    DO_NOT_DISTURB(ScenarioName.DO_NOT_DISTURB, new DoNotDisturbScenario(), Material.DIAMOND_CHESTPLATE, "Once you hit a player, you are linked to them for 30 seconds and your fight cannot be interrupted."),
    DIAMOND_LESS(ScenarioName.DIAMOND_LESS, new DiamondlessScenario(), Material.DIAMOND, "You cannot mine diamonds. Players drop 1 diamond on death."),
    ORE_FRENZY(ScenarioName.ORE_FRENZY, new OreFrenzyScenario(), Material.LAPIS_ORE, "Mining a lapis ore will give you a splash potion of healing. Mining an emerald ore will give you 32 arrows. Mining a redstone ore will give you a book. Mining a diamond ore will give you a diamond and 4 bottles of XP. Mining a quartz ore will give you a block of TNT."),
    BARE_BONES(ScenarioName.BARE_BONES, new BarebonesScenario(), Material.BONE, "Iron is the highest tier you can obtain through gearing up. You cannot craft enchantment tables, anvils or golden apples. Players drop 1 diamond, 1 golden apple, 32 arrows and 2 string on death. The nether is disabled."),
    GOLD_LESS(ScenarioName.GOLD_LESS, new GoldlessScenario(), Material.GOLD_INGOT, "You cannot mine gold. Players drop 8 gold ingots and a Golden Head on death."),
    INSTANT_INV(ScenarioName.INSTANT_INV, new InstantInvScenario(), Material.GOLD_ORE, "All mined ores and experience automatically go to your inventory."),
    LUCKY_LEAVES(ScenarioName.LUCKY_LEAVES, new LuckyLeavesScenario(), Material.LEAVES, "There is a low chance of golden apples dropping from trees."),
    TRIPLE_ORES(ScenarioName.TRIPLE_ORES, new TripleOresScenario(), Material.NETHER_STAR, "All mined ores give 3x the items."),
    DOUBLE_ORES(ScenarioName.DOUBLE_ORES, new DoubleOresScenario(), Material.NETHER_BRICK, "All mined ores give 2x the items."),
    OP_HASTEY_BOY(ScenarioName.OP_HASTEY_BOYS, new OPHasteyBoysScenario(), Material.DIAMOND_PICKAXE, "Crafted tools are enchanted with Efficiency V and Unbreaking V."),
    BROADCASTER(ScenarioName.BROADCASTER, new BroadcasterScenario(), Material.COMPASS, "There is a 20% chance that your coordinates are broadcast upon mining a diamond ore."),
    RADAR(ScenarioName.RADAR, new RadarScenario(), Material.COMPASS, "You can track opponents and their distance by using a compass you receive once the grace period has ended."),
    NO_FALL(ScenarioName.NO_FALL, new NoFallScenario(), Material.DIAMOND_BOOTS, "You cannot take fall damage."),
    BOW_LESS(ScenarioName.BOW_LESS, new BowlessScenario(), Material.BOW, "You cannot craft or use bows."),
    ROD_LESS(ScenarioName.ROD_LESS, new RodlessScenario(), Material.FISHING_ROD, "You cannot craft or use fishing rods."),
    FIRE_LESS(ScenarioName.FIRE_LESS, new FirelessScenario(), Material.FLINT_AND_STEEL, "You cannot take damage from lava or fire. This scenario does NOT work in the nether!"),
    FLOWER_POWER(ScenarioName.FLOWER_POWER, new FlowerPowerScenario(), Material.YELLOW_FLOWER, "You receive random items from flowers."),
    SOUP(ScenarioName.SOUP, new SoupScenario(), Material.MUSHROOM_SOUP, "You instantly regain 2 hearts when right clicking a soup."),
    HORSE_LESS(ScenarioName.HORSE_LESS, new HorselessScenario(), Material.SADDLE, "You cannot ride horses."),
    BACKPACKS(ScenarioName.BACKPACKS, new BackpackScenario(), Material.ENDER_CHEST, "All players have a backpack inventory which can be shared with their team."),
    BLEEDING_SWEETS(ScenarioName.BLEEDING_SWEETS, new BleedingSweetsScenario(), Material.DIAMOND, "Players drop 1 diamond, 1 book, 1 string, 5 gold and 16 arrows on death."),
    BLOOD_DIAMONDS(ScenarioName.BLOOD_DIAMONDS, new BloodDiamondsScenario(), Material.DIAMOND_ORE, "You lose half a heart for every diamond you mine."),
    BLOOD_ENCHANTS(ScenarioName.BLOOD_ENCHANTS, new BloodEnchantsScenario(), Material.ENCHANTMENT_TABLE, "You lose half a heart for every level you enchant."),
    GOLDEN_RETRIEVER(ScenarioName.GOLDEN_RETRIEVER, new GoldenRetrieverScenario(), Material.GOLDEN_APPLE, "Players drop 1 Golden Head on death."),
    VANILLA_PLUS(ScenarioName.VANILLA_PLUS, new VanillaPlusScenario(), Material.FLINT, "Flint rate is increased to 70%. Apple rate is increased to 3%."),
    VEIN_MINER(ScenarioName.VEIN_MINER, new VeinMinerScenario(), Material.DIAMOND_ORE, "The entire ore vein is mined when breaking an ore."),
    WEB_CAGE(ScenarioName.WEB_CAGE, new WebcageScenario(), Material.WEB, "Upon killing a player, a sphere of cobwebs will surround their corpse."),
    LIMITATIONS(ScenarioName.LIMITATIONS, new LimitationsScenario(), Material.REDSTONE, "You can only mine 16 diamonds, 32 gold and 64 iron."),
    COLD_WEAPONS(ScenarioName.COLD_WEAPONS, new ColdWeaponsScenario(), Material.IRON_SWORD, "Fire Aspect and Flame enchantments are disabled."),
    LIMITED_ENCHANTS(ScenarioName.LIMITED_ENCHANTS, new LimitedEnchantsScenario(), Material.ENCHANTMENT_TABLE, "You cannot craft enchantment tables. There is an unbreakable enchantment table at 0,0. There are unbreakable enchantment tables at +/- of half the starting border.");

    private final ScenarioName name;
    private final ScenarioListener listener;
    private final Material material;
    private final List<String> description;

    @Setter
    private boolean enabled;

    Scenario(ScenarioName name, ScenarioListener listener, Material material, String description) {
        this.name = name;
        this.listener = listener;
        this.material = material;
        this.description = ItemBuilder.wrapLore(description, 32);
        this.listener.setPlugin(UHC.get());
    }

    public boolean isDisabled() {
        return !enabled;
    }

    public void update(boolean enable) {
        if ((enable && !isEnabled()) || !enable && isEnabled()) {
            toggle();
        }
    }

    public void toggle() {
        if (isEnabled()) {
            disable();
        } else {
            enable();
        }
    }

    public void enable() {
        if (isEnabled()) return;

        enabled = true;
        Bukkit.getPluginManager().registerEvents(listener, UHC.get());
        listener.onEnable();
        MessageUtil.send(CC.BOLD_GOLD + "[Scenario] " + CC.GRAY + name + CC.GREEN + " (" + Lang.CHECKMARK + ')', Sound.NOTE_PIANO);
    }

    public void disable() {
        if (isDisabled()) return;

        enabled = false;
        HandlerList.unregisterAll(listener);
        listener.onDisable();
        MessageUtil.send(CC.BOLD_GOLD + "[Scenario] " + CC.GRAY + name + CC.RED + " (" + Lang.X + ')', Sound.DIG_GRASS);
    }

    public ItemStack getItem(boolean editor) {
        return new ItemBuilder()
                .setMaterial(material)
                .setName(CC.PINK + name)
                .setLore(() -> {
                    List<String> lore = new ArrayList<>(List.of(""));
                    description.forEach(line -> lore.add(CC.GRAY + line));

                    if (editor) {
                        lore.add("");
                        lore.add(enabled
                                ? CC.GREEN + "This scenario is active."
                                : CC.RED + "This scenario is not active."
                        );
                    }
                    return lore;
                })
                .build();
    }

    public static List<Scenario> getEnabledScenarios() {
        return getAllScenarios()
                .stream()
                .filter(Scenario::isEnabled)
                .collect(Collectors.toList());
    }

    public static List<Scenario> getAllScenarios() {
        return List.of(values());
    }
}
