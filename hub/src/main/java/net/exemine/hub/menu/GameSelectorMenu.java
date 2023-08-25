package net.exemine.hub.menu;

import net.exemine.api.instance.Instance;
import net.exemine.api.instance.InstanceService;
import net.exemine.api.match.impl.uhc.UHCMatch;
import net.exemine.api.model.ScenarioName;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.menu.Menu;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.hub.user.HubUser;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GameSelectorMenu extends Menu<HubUser> {

    private final InstanceService instanceService;

    public GameSelectorMenu(HubUser user) {
        super(user, CC.DARK_GRAY + "Game Selector", 3);
        this.instanceService = user.getPlugin().getCore().getInstanceService();
        setAutoFill(true);
        setAutoUpdate(true);
    }

    @Override
    public void update() {
        addExitItem();

        String uhcInstanceName = "UHC";
        Instance uhcInstance = instanceService.getInstance(uhcInstanceName);

        set(12, new ItemBuilder(Material.GOLDEN_APPLE)
                .setName(CC.BOLD_PINK + uhcInstanceName)
                .setLore(lore -> {
                    lore.add(CC.DARK_GRAY + "Ultra Hardcore");
                    lore.add(" ");
                    lore.add(CC.GRAY + "Gear up and fight to be the last");
                    lore.add(CC.GRAY + "man/team standing without natural");
                    lore.add(CC.GRAY + "regeneration, a closing border and");
                    lore.add(CC.GRAY + "different scenarios!");
                    lore.addAll(getUHCLore(uhcInstance));
                })
                .build()
        ).onClick(() -> user.performCommand("join " + uhcInstanceName));

        String ffaInstanceName = "FFA";
        Instance ffaInstance = instanceService.getInstance(ffaInstanceName);

        set(14, new ItemBuilder(Material.DIAMOND_SWORD)
            .setName(CC.BOLD_PINK + ffaInstanceName)
            .setLore(lore -> {
                lore.add(CC.DARK_GRAY + "Free For All");
                lore.add(" ");
                lore.add(CC.GRAY + "Looking for a place to warm up");
                lore.add(CC.GRAY + "for UHC matches? Try out our FFA");
                lore.add(CC.GRAY + "map and gain the highest kill");
                lore.add(CC.GRAY + "streak! Teaming is not allowed.");
                lore.add("");
                if (ffaInstance.isOffline()) {
                    lore.add(CC.RED + "Server is currently offline.");
                    lore.add(CC.RED + "Please try again later!");
                } else {
                    lore.add(CC.GRAY + "Players: " + CC.RESET + ffaInstance.getOnlinePlayers() +
                        "/" + ffaInstance.getMaxPlayers());
                    lore.add("");
                    lore.add(CC.GREEN + "Click to join!");
                }
            })
            .build()
        ).onClick(() -> user.performCommand("join " + ffaInstanceName));
    }

    public List<String> getUHCLore(Instance instance) {
        List<String> lore = new ArrayList<>();
        UHCMatch match = GsonUtil.fromJsonObject(instance.getExtra(), UHCMatch.class);

        if (instance.isOffline() || match == null) {
            lore.add("");
            lore.add(CC.RED + "Game information isn't available.");
            lore.add(CC.RED + "Please try again later!");
            return lore;
        }
        UHCMatch.RunningInfo runningInfo = match.getRunningInfo();
        Set<ScenarioName> scenarios = match.getScenarios();
        String state = runningInfo.getGameState();

        switch (state.toUpperCase()) {
            case "LOBBY":
                lore.add("");
                lore.add(CC.BOLD_PINK + "Lobby Information");
                lore.add(CC.GRAY + "Mode: " + CC.RESET + match.getMode());
                lore.add(CC.GRAY + "Border: " + CC.RESET + match.getInitialBorder());
                lore.add(CC.GRAY + "Nether: " + CC.RESET + (match.isNether() ? "Enabled" : "Disabled"));
                lore.add(CC.GRAY + "Spectating: " + CC.RESET + (match.isSpectating() ? "Enabled" : "Disabled"));

                long startTime = match.getStartTime();
                if (startTime != 0L) {
                    if (startTime > System.currentTimeMillis()) {
                        lore.add(CC.GRAY + "Whitelist off in: " + CC.RESET + TimeUtil.getNormalDuration(startTime - System.currentTimeMillis()));
                    } else {
                        lore.add(CC.GRAY + "Starting in: " + CC.RESET + TimeUtil.getNormalDuration(startTime + TimeUtil.MINUTE * 10 - System.currentTimeMillis()));
                    }
                }

                lore.add("");
                lore.add(CC.GRAY + "Scenarios:");

                if (scenarios.isEmpty()) {
                    lore.add(Lang.LIST_PREFIX + CC.RESET + " Vanilla");
                } else {
                    scenarios.forEach(scenario -> lore.add(Lang.LIST_PREFIX + CC.RESET + scenario.getName()));
                }
                break;
            case "SCATTERING":
                lore.add("");
                lore.add(CC.BOLD_PINK + "Scatter Information");
                lore.add(CC.GRAY + "Scattering: " + CC.RESET + runningInfo.getScatteringCount());
                lore.add(CC.GRAY + "Scattered: " + CC.RESET + runningInfo.getScatteredCount());
                break;
            case "PLAYING":
                lore.add("");
                lore.add(CC.BOLD_PINK + "Game Information");
                lore.add(CC.GRAY + "Game Time: " + CC.RESET + TimeUtil.getClockTime(System.currentTimeMillis() - match.getStartTime()));
                lore.add(CC.GRAY + "Alive Players: " + CC.RESET + runningInfo.getAlivePlayerCount() + "/" + match.getInitialPlayerCount());
                if (match.isTeamGame())
                    lore.add(CC.GRAY + "Alive Teams: " + CC.RESET + runningInfo.getAliveTeamCount());
                lore.add(CC.GRAY + "Spectators: " + CC.RESET + runningInfo.getSpectatorCount());
                lore.add("");
                lore.add(CC.GRAY + "Border: " + CC.RESET + runningInfo.getCurrentBorder());
                lore.add(CC.GRAY + "Nether: " + CC.RESET + (match.isNether() ? "Enabled" : "Disabled"));
                lore.add(CC.GRAY + "Spectating: " + CC.RESET + (match.isSpectating() ? "Enabled" : "Disabled"));
                lore.add("");
                lore.add(CC.GRAY + "Scenarios:");

                if (scenarios.isEmpty()) {
                    lore.add(Lang.LIST_PREFIX + CC.WHITE + "Vanilla");
                } else {
                    scenarios.forEach(scenario -> lore.add(Lang.LIST_PREFIX + CC.RESET + scenario.getName()));
                }
                break;
            case "ENDING":
                lore.add("");
                Set<String> winnerNamesColored = runningInfo.getWinnerNames();
                lore.add(CC.BOLD_PINK + "Winner Information");
                if (match.isTeamGame()) {
                    lore.add("");
                    lore.add(CC.GRAY + "Winners:");
                    winnerNamesColored.forEach(name -> lore.add(Lang.LIST_PREFIX + CC.WHITE + name));
                    lore.add("");
                    lore.add(CC.GRAY + "Total Kills: " + CC.RESET + runningInfo.getWinningTeamKills());
                } else {
                    lore.add(CC.GRAY + "Winner: " + CC.RESET + String.join("", winnerNamesColored));
                    lore.add(CC.GRAY + "Kills: " + CC.RESET + runningInfo.getWinningTeamKills());
                }
        }
        lore.add("");
        lore.add(CC.BOLD_PINK + "Global Information");
        lore.add(CC.GRAY + "State: " + CC.RESET + (instance.isWhitelisted() ? instance.getStatus(user.isEqualOrAbove(RankType.STAFF))
                : runningInfo.isWhitelisted() ? CC.RED + "Whitelisted"
                : StringUtil.formatEnumName(state)));
        lore.add(CC.GRAY + "Players: " + CC.RESET + instance.getOnlinePlayers() + "/" + instance.getMaxPlayers());
        lore.add("");
        lore.add(instance.isWhitelisted() || runningInfo.isWhitelisted() ? CC.RED + "Server is whitelisted!"
                : state.equalsIgnoreCase("LOBBY") || state.equalsIgnoreCase("SCATTERING") ? CC.GREEN + "Click to play!"
                : !match.isSpectating() ? CC.RED + "Spectating is disabled!"
                : CC.GREEN + "Click to spectate!"
        );
        return lore;
    }
}
