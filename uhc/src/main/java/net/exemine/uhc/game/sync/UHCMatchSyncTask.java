package net.exemine.uhc.game.sync;

import net.exemine.api.match.MatchState;
import net.exemine.api.match.impl.uhc.UHCMatch;
import net.exemine.core.util.InstanceUtil;
import net.exemine.uhc.UHC;
import net.exemine.uhc.border.BorderService;
import net.exemine.uhc.config.option.ToggleOption;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.scenario.Scenario;
import net.exemine.uhc.team.Team;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class UHCMatchSyncTask extends BukkitRunnable {

    private final UHC plugin;

    private boolean matchEnded = false;

    public UHCMatchSyncTask(UHC plugin) {
        this.plugin = plugin;
        runTaskTimerAsynchronously(plugin, 0L, 20L);
    }

    @Override
    public void run() {
        GameService gameService = plugin.getGameService();
        UHCMatch match = gameService.getMatch();
        boolean updateInDatabase = true;

        // Actual match is created on game start
        if (match == null) {
            updateInDatabase = false;
            match = new UHCMatch();
        }
        UHCUserService userService = plugin.getUserService();
        BorderService borderService = plugin.getBorderService();

        UHCMatch.RunningInfo runningInfo = match.getRunningInfo();
        UHCUser host = gameService.getHost();
        UHCUser supervisor = gameService.getSupervisor();

        match.setHostUuid(host == null ? null : host.getUniqueId());
        match.setSupervisorUuid(supervisor == null ? null : supervisor.getUniqueId());
        match.setMode(gameService.getFormattedMode());
        match.setNether(ToggleOption.NETHER.isEnabled());
        match.setSpectating(ToggleOption.SPECTATING.isEnabled());
        match.setScenarios(Scenario.getEnabledScenarios()
                .stream()
                .map(Scenario::getName)
                .collect(Collectors.toSet()));
        match.setThanksCount((int) userService.values()
                .stream()
                .filter(UHCUser::isHostThanks)
                .count());
        runningInfo.setGameState(gameService.getState().name());
        runningInfo.setWhitelisted(ToggleOption.WHITELIST.isEnabled());

        switch (gameService.getState()) {
            case LOBBY:
                match.setInitialBorder(borderService.getCurrentRadius().getValue());
                break;
            case SCATTERING:
                runningInfo.setScatteringCount(userService.getWaitingUsers().size());
                runningInfo.setScatteredCount(userService.getAllScatterUsers().size());
                break;
            case PLAYING:
                match.setState(MatchState.IN_PROGRESS);
                match.setInitialPlayerCount(gameService.getInitialPlayers());
                runningInfo.setAlivePlayerCount(userService.getInGameUsers().size());
                runningInfo.setAliveTeamCount(plugin.getTeamService().getAliveTeams().size());
                runningInfo.setSpectatorCount(userService.getSpectatorUsers().size());
                runningInfo.setCurrentBorder(borderService.getCurrentRadius().getValue());

                if (match.getInitialBorder() == 0) {
                    match.setInitialBorder(borderService.getCurrentRadius().getValue());
                }

                Set<UUID> participants = match.getParticipants();
                userService.getInGameUsers().forEach(inGameUser -> {
                    if (participants.contains(inGameUser.getUniqueId())) return;
                    participants.add(inGameUser.getUniqueId());
                });
                break;
            case ENDING:
                match.setDuration(gameService.getEndTime() - gameService.getStartTime());
                match.setState(ToggleOption.STATS.isEnabled()
                        ? MatchState.RANKED
                        : MatchState.UNRANKED
                );
                List<Team> aliveTeams = plugin.getTeamService().getAliveTeams();
                if (aliveTeams.size() == 1) {
                    Team winningTeam = aliveTeams.get(0);
                    match.setWinnerUuids(winningTeam.getMembers()
                            .stream()
                            .map(UHCUser::getUniqueId)
                            .collect(Collectors.toSet()));
                    runningInfo.setWinnerNames(winningTeam.getMembers()
                            .stream()
                            .map(UHCUser::getRealName)
                            .collect(Collectors.toSet()));
                    runningInfo.setWinningTeamKills(winningTeam.getTotalKills());
                }
                // Do not update anymore after the game has ended, so we don't "override" database
                // changes
                if (!matchEnded) {
                    plugin.getMatchService().updateMatch(match);
                }
                matchEnded = true;
        }
        InstanceUtil.getCurrent().setExtra(match);

        if (updateInDatabase && gameService.getState() != GameState.ENDING) {
            plugin.getMatchService().updateMatch(match);
        }
    }
}
