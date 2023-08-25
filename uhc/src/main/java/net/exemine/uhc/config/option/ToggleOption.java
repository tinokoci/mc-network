package net.exemine.uhc.config.option;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.util.MessageUtil;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.uhc.UHC;
import net.exemine.uhc.assign.AutoAssignListener;
import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.config.impl.AbsorptionListener;
import net.exemine.uhc.config.impl.AntiBurnListener;
import net.exemine.uhc.config.impl.AutoBorderListener;
import net.exemine.uhc.config.impl.AutoRespawnListener;
import net.exemine.uhc.config.impl.BedBombingListener;
import net.exemine.uhc.config.impl.EnderPearlDamageListener;
import net.exemine.uhc.config.impl.FlatFiftyBorderListener;
import net.exemine.uhc.config.impl.FlatTwentyFiveBorderListener;
import net.exemine.uhc.config.impl.GodApplesListener;
import net.exemine.uhc.config.impl.GoldenHeadsListener;
import net.exemine.uhc.config.impl.HorseHealingListener;
import net.exemine.uhc.config.impl.HorsesListener;
import net.exemine.uhc.config.impl.IPvPListener;
import net.exemine.uhc.config.impl.InvisibilityPotionsListener;
import net.exemine.uhc.config.impl.NetherBeforePvPListener;
import net.exemine.uhc.config.impl.NetherListener;
import net.exemine.uhc.config.impl.PoisonPotionsListener;
import net.exemine.uhc.config.impl.SpeedOnePotionsListener;
import net.exemine.uhc.config.impl.SpeedTwoPotionsListener;
import net.exemine.uhc.config.impl.StatsListener;
import net.exemine.uhc.config.impl.StrengthOnePotionsListener;
import net.exemine.uhc.config.impl.StrengthTwoPotionsListener;
import net.exemine.uhc.config.impl.TeamDamageListener;
import net.exemine.uhc.practice.PracticeListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum ToggleOption {

    ABSORPTION("Absorption", Material.GOLDEN_APPLE, new AbsorptionListener(), true, false, "Whether the Absorption effect is enabled."),
    ANTI_BURN("Anti Burn", Material.FLINT_AND_STEEL, new AntiBurnListener(), true, true, "Whether burned items of a dead player are given to their killer."),
    AUTO_ASSIGN("Auto Assign", Material.GOLD_CHESTPLATE, new AutoAssignListener(), false, true, "Whether fights are automatically assigned between the remaining teams in 50x50 or lower."),
    AUTO_RESPAWN("Auto Respawn", Material.ARROW, new AutoRespawnListener(), true, true, "Whether players that died to cheaters are automatically respawned when their killer is banned."),
    NETHER("Nether", Material.NETHER_BRICK, new NetherListener(), true, false, "Whether players are able to enter the nether."),
    NETHER_BEFORE_PVP("Nether Before PvP", Material.NETHER_FENCE, new NetherBeforePvPListener(), false, false, "Whether the nether can be entered before PvP is enabled."),
    BED_BOMBING("Bed Bombing", Material.BED, new BedBombingListener(), false, false, "Whether beds explode in nether upon right-clicking them."),
    ENDER_PEARL_DAMAGE("Ender Pearl Damage", Material.ENDER_PEARL, new EnderPearlDamageListener(), false, false, "Whether players take damage from throwing ender pearls."),
    AUTO_BORDER("Auto Border", Material.BEDROCK, new AutoBorderListener(), true, true, "Whether the border radius automatically adjusts to the amount of participants."),
    FLAT_25_BORDER("Flat 25 Border", Material.GRASS, new FlatTwentyFiveBorderListener(), false, true, "Whether the 25x25 border shrink creates a flat area."),
    FLAT_50_BORDER("Flat 50 Border", Material.GRASS, new FlatFiftyBorderListener(), false, true, "Whether the 50x50 border shrink creates a flat area."),
    GOD_APPLES("God Apples", Material.GOLDEN_APPLE, 1, new GodApplesListener(), false, false, "Whether god apples can be crafted."),
    GOLDEN_HEADS("Golden Heads", Material.GOLDEN_APPLE, new GoldenHeadsListener(), true, true, "Whether Golden Heads can be crafted."),
    HORSE_HEALING("Horse Healing", Material.HAY_BLOCK, new HorseHealingListener(), true, false, "Whether horses regenerate health."),
    HORSES("Horses", Material.SADDLE, new HorsesListener(), true, false, "Whether players are able to ride horses."),
    IPVP("iPvP", Material.WATCH, new IPvPListener(), false, false, "Whether players can indirectly damage other participants before PvP is enabled."),
    STATS("Stats", Material.BOOK, new StatsListener(), true, true, "Whether statistics will be saved for this game."),
    INVISIBILITY_POTIONS("Invisibility Potions", Material.POTION, 8238, new InvisibilityPotionsListener(), false, false, "Whether players are able to use Invisibility potions."),
    POISON_POTIONS("Poison Potions", Material.POTION, 8196, new PoisonPotionsListener(), false, false, "Whether players are able to use Poison potions."),
    RANDOM_TELEPORT("Random Teleport", Material.WORKBENCH, false, "Whether players outside the border are randomly teleported upon border shrink. The 100x100 border shrink always teleports outside players randomly."),
    SPEED_POTIONS_I("Speed Potions I", Material.POTION, 8194, new SpeedOnePotionsListener(), true, false, "Whether players are able to use Speed I potions."),
    SPEED_POTIONS_II("Speed Potions II", Material.POTION, 8226, new SpeedTwoPotionsListener(), true, false, "Whether players are able to use Speed II potions."),
    STRENGTH_POTIONS_I("Strength Potions I", Material.POTION, 8201, new StrengthOnePotionsListener(), false, false, "Whether players are able to use Strength I potions."),
    STRENGTH_POTIONS_II("Strength Potions II", Material.POTION, 8233, new StrengthTwoPotionsListener(), false, false, "Whether players are able to use Strength II potions."),
    TEAM_DAMAGE("Team Damage", Material.GOLD_HELMET, new TeamDamageListener(), true, false, "Whether players are able to damage their team mates."),
    PRACTICE("Practice", Material.IRON_SWORD, new PracticeListener(), true, true, "Whether players are able to warm up in the practice arena before start."),
    WHITELIST("Whitelist", Material.BOOK, "Whether the server is whitelisted."),
    SPECTATING("Spectating", Material.COMPASS, "Whether players can watch the game as spectators.");

    private final String name;
    private final Material material;
    private final int itemDurability;
    private final ConfigListener listener;
    private final List<String> description;
    private final boolean registerListenerIfEnabled;

    @Setter
    private boolean enabled;

    ToggleOption(String name, Material material, int itemDurability, ConfigListener listener, boolean enableAsDefault, boolean registerListenerIfEnabled, String description) {
        this.name = name;
        this.material = material;
        this.itemDurability = itemDurability;
        this.listener = listener;
        this.enabled = enableAsDefault;
        this.registerListenerIfEnabled = registerListenerIfEnabled;
        this.description = ItemBuilder.wrapLore(description, 32);
    }

    ToggleOption(String name, Material material, ConfigListener listener, boolean enableAsDefault, boolean registerListenerIfEnabled, String description) {
        this(name, material, -1, listener, enableAsDefault, registerListenerIfEnabled, description);
    }

    ToggleOption(String name, Material material, boolean enableAsDefault, String description) {
        this(name, material, null, enableAsDefault, true, description);
    }

    ToggleOption(String name, Material material, String description) {
        this(name, material, null, true, true, description);
    }

    public void initialize(UHC plugin) {
        if (listener != null) {
            listener.setPlugin(plugin);
            toggleListener(enabled);
        }
    }

    public boolean isDisabled() {
        return !isEnabled();
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
        toggleListener(true);
        MessageUtil.send(CC.BOLD_GOLD + "[Option] " + CC.GRAY + name + CC.GREEN + " (" + Lang.CHECKMARK + ')', Sound.NOTE_PIANO);
    }

    public void disable() {
        if (isDisabled()) return;

        enabled = false;
        toggleListener(false);
        MessageUtil.send(CC.BOLD_GOLD + "[Option] " + CC.GRAY + name + CC.RED + " (" + Lang.X + ')', Sound.DIG_GRASS);
    }

    private void toggleListener(boolean enable) {
        if (listener == null) return;
        if (!registerListenerIfEnabled) enable = !enable;

        if (enable) {
            listener.onEnable();
            Bukkit.getPluginManager().registerEvents(listener, UHC.get());
        } else {
            listener.onDisable();
            HandlerList.unregisterAll(listener);
        }
    }

    public ItemStack getItem() {
        return new ItemBuilder()
                .setMaterial(material)
                .setDurability(itemDurability)
                .setName(CC.PINK + name)
                .setLore(() -> {
                    List<String> lore = new ArrayList<>(List.of(""));
                    description.forEach(line -> lore.add(CC.GRAY + line));
                    lore.add("");
                    lore.add(enabled
                            ? CC.GREEN + "This option is active."
                            : CC.RED + "This option is not active."
                    );
                    return lore;
                })
                .build();
    }
}
