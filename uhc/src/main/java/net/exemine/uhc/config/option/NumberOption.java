package net.exemine.uhc.config.option;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.util.string.CC;
import net.exemine.core.util.MessageUtil;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.uhc.UHC;
import net.exemine.uhc.game.GameState;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter

public enum NumberOption {

    PLAYERS_PER_TEAM("Players Per Team", 1, Material.PAPER, "The maximum allowed team size."),
    BORDER_SHRINK_INTERVAL("Border Shrink Interval", 5, Material.WATCH, "The interval between border shrinks."),
    BORDER_SHRINK_START("Border Shrink Start", 30, Material.BEDROCK, "When the border starts shrinking."),
    FINAL_HEAL("Final Heal", 10, Material.SPECKLED_MELON, "When all players receive their final heal."),
    GRACE_PERIOD("Grace Period", 20, Material.DIAMOND_SWORD, "When the grace period ends and PvP is enabled."),
    APPLE_RATE("Apple Rate", 3, Material.APPLE, "The chance of apples dropping due to leaves decay or shears."),
    PRACTICE_SLOTS("Practice Slots", 150, Material.FEATHER, "The maximum amount of players that can join the practice arena."),
    RELOG_TIME("Relog Time", 10, Material.EMERALD, "The maximum time players can spend disconnected during the game."),
    SLOTS("Slots", 200, Material.SKULL_ITEM, "The maximum amount of players able to participate in the game."),
    STARTER_FOOD("Starter Food", 16, Material.COOKED_BEEF, "The amount of steak players receive at start.");

    private final String name;
    private final Material material;
    private final List<String> description;

    private int value;

    NumberOption(String name, int value, Material material, String description) {
        this.name = name;
        this.value = value;
        this.material = material;
        this.description = ItemBuilder.wrapLore(description, 32);
    }

    public boolean hasValue(int value) {
        return this.value == value;
    }

    public long getMinutesInMillis() {
        return getMinutesInSeconds() * 1000L;
    }

    public int getMinutesInSeconds() {
        return value * 60;
    }

    public ItemStack getItem(boolean editor) {
        return new ItemBuilder()
                .setMaterial(material)
                .setName(CC.PINK + name)
                .setLore(() -> {
                    List<String> lore = new ArrayList<>(List.of(""));
                    description.forEach(line -> lore.add(CC.GRAY + line));
                    lore.add("");
                    lore.add(CC.GOLD + "Currently: " + CC.WHITE + value);

                    if (editor) {
                        lore.add("");
                        if (canBeEdited()) {
                            lore.add(CC.GREEN + "Click to edit.");
                        } else {
                            lore.add(CC.RED + "This cannot be edited.");
                        }
                    }
                    return lore;
                })
                .build();
    }

    public void setValue(int value) {
        setValue(value, true);
    }

    public void setValue(int value, boolean broadcast) {
        if (hasValue(value) || !canBeEdited()) return;

        if (this == PLAYERS_PER_TEAM) {
            UHC.get().getTeamService().clearTeams();
        }
        this.value = value;
        if (broadcast) {
            MessageUtil.send(CC.BOLD_GOLD + "[Option] " + CC.PINK + name + CC.GRAY + " has been updated with the value " + CC.WHITE + value + CC.GRAY + '.');
        }
    }

    public boolean canBeEdited() {
        return this != PLAYERS_PER_TEAM || UHC.get().getGameService().isStateOrLower(GameState.LOBBY);
    }
}
