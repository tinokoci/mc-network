package net.exemine.uhc.config.menu;

import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.menu.Menu;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.uhc.UHC;
import net.exemine.uhc.border.BorderService;
import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.config.option.ToggleOption;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.scenario.Scenario;
import net.exemine.uhc.scenario.menu.ScenarioViewMenu;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class ConfigViewMenu extends Menu<UHCUser> {

    private final BorderService borderService;
    private final GameService gameService;

    public ConfigViewMenu(UHCUser user, UHC plugin) {
        super(user, CC.DARK_GRAY + "Game Information", 6);
        this.borderService = plugin.getBorderService();
        this.gameService = plugin.getGameService();
    }

    @Override
    public void update() {
        // Left Upper Corner
        set(0, ToggleOption.SPEED_POTIONS_I.getItem());
        set(1, ToggleOption.SPEED_POTIONS_II.getItem());
        set(9, ToggleOption.STRENGTH_POTIONS_I.getItem());
        set(10, ToggleOption.STRENGTH_POTIONS_II.getItem());
        set(18, ToggleOption.INVISIBILITY_POTIONS.getItem());
        set(19, ToggleOption.POISON_POTIONS.getItem());

        // Right Upper Corner
        set(7, ToggleOption.FLAT_25_BORDER.getItem());
        set(16, ToggleOption.FLAT_50_BORDER.getItem());
        set(25, ToggleOption.RANDOM_TELEPORT.getItem());
        set(8, ToggleOption.NETHER.getItem());
        set(17, ToggleOption.NETHER_BEFORE_PVP.getItem());
        set(26, ToggleOption.BED_BOMBING.getItem());

        // Left Down Corner
        set(36, ToggleOption.AUTO_ASSIGN.getItem());
        set(45, ToggleOption.ABSORPTION.getItem());
        set(37, ToggleOption.ANTI_BURN.getItem());
        set(46, ToggleOption.GOLDEN_HEADS.getItem());
        set(47, ToggleOption.GOD_APPLES.getItem());

        // Right Down Corner
        set(43, ToggleOption.ENDER_PEARL_DAMAGE.getItem());
        set(51, NumberOption.STARTER_FOOD.getItem(false));
        set(44, ToggleOption.TEAM_DAMAGE.getItem());
        set(52, ToggleOption.HORSES.getItem());
        set(53, ToggleOption.HORSE_HEALING.getItem());

        // Middle
        set(12, NumberOption.GRACE_PERIOD.getItem(false));
        set(21, NumberOption.PLAYERS_PER_TEAM.getItem(false));
        set(13, NumberOption.FINAL_HEAL.getItem(false));
        set(23, ToggleOption.STATS.getItem());

        set(22, new ItemBuilder()
                .setMaterial(Material.BEACON)
                .setName(CC.PINK + "Scenarios")
                .setLore(() -> {
                    List<String> lore = new ArrayList<>();
                    List<Scenario> enabledScenarios = Scenario.getEnabledScenarios();

                    if (enabledScenarios.isEmpty()) {
                        lore.add(Lang.LIST_PREFIX + CC.WHITE + "Vanilla");
                    } else {
                        lore.add("");
                        enabledScenarios.forEach(scenario -> lore.add(Lang.LIST_PREFIX + CC.WHITE + scenario.getName()));
                        lore.add("");
                        lore.add(CC.GREEN + "Click to see the explanation.");
                    }
                    return lore;
                })
                .build()
        ).onClick(() -> {
            if (Scenario.getEnabledScenarios().isEmpty()) return;
            new ScenarioViewMenu(user).open();
        });
        set(14, new ItemBuilder()
                .setMaterial(Material.BEDROCK)
                .setName(CC.PINK + "Border")
                .setLore(() -> {
                    List<String> lore = new ArrayList<>(List.of(""));
                    lore.add(CC.GRAY + "Information about the game border.");
                    lore.add("");
                    lore.add(CC.GOLD + "Current Radius: " + CC.WHITE + borderService.getCurrentRadius().getValue());
                    lore.add(CC.GOLD + "First Border Shrink: " + CC.WHITE + (NumberOption.BORDER_SHRINK_START.getValue() + NumberOption.BORDER_SHRINK_INTERVAL.getValue()) + " minutes");
                    lore.add(CC.GOLD + "Border Shrink Interval: " + CC.WHITE + NumberOption.BORDER_SHRINK_INTERVAL.getValue() + " minutes");
                    lore.add(CC.GOLD + "Auto Border: " + (ToggleOption.AUTO_BORDER.isEnabled() ? CC.GREEN + "Active" : CC.RED + "Not active"));
                    return lore;
                })
                .build()
        );
        set(31, new ItemBuilder(Material.ENCHANTMENT_TABLE)
                .setName(CC.PINK + "Host")
                .setLore(() -> {
                    List<String> lore = new ArrayList<>(List.of(""));
                    lore.add(CC.GRAY + "Who is hosting the game?");
                    lore.add("");
                    lore.add(CC.GOLD + "Host: " + CC.WHITE + gameService.getFormattedHost());

                    if (gameService.getSupervisor() != null) {
                        lore.add(CC.GOLD + "Supervisor: " + gameService.getFormattedSupervisor());
                    }
                    return lore;
                })
                .build()
        );
    }
}
