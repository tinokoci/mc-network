package net.exemine.uhc.provider;

import lombok.RequiredArgsConstructor;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.provider.ScoreboardProvider;
import net.exemine.core.provider.scoreboard.PlayerScoreboard;
import net.exemine.core.util.InstanceUtil;
import net.exemine.uhc.border.BorderService;
import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.scatter.ScatterService;
import net.exemine.uhc.scenario.Scenario;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import net.exemine.uhc.user.UHCUserState;
import net.exemine.uhc.user.info.DoNotDisturbInfo;
import net.exemine.uhc.user.info.GameInfo;
import net.exemine.uhc.user.info.NoCleanInfo;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UHCScoreboardProvider implements ScoreboardProvider<UHCUser> {

    private final BorderService borderService;
    private final GameService gameService;
    private final ScatterService scatterService;
    private final UHCUserService userService;

    private List<Scenario> enabledScenarios;
    private List<Scenario> shownScenarios;

    @Override
    public void beforeUpdate() {
        enabledScenarios = Scenario.getEnabledScenarios();
        shownScenarios = enabledScenarios.stream().limit(gameService.isScheduledToAutoStart() ? 3 : 4).collect(Collectors.toList());
    }

    @Override
    public String getTitle() {
        return CC.BOLD_PURPLE + Lang.SERVER_NAME + ' ' + CC.GRAY + '(' + InstanceUtil.getType().getName() + ')';
    }

    @Override
    public void update(UHCUser user, PlayerScoreboard<UHCUser> board) {
        board.add(CC.RED + CC.STRIKETHROUGH_GRAY + "--------------------");

        switch (gameService.getState()) {
            case WORLD_GENERATION:
                board.add(CC.BOLD_PINK + "World Generation");
                board.add(CC.GRAY + "Please be patient...");
                break;
            case LOBBY:
                board.add("Online: " + CC.GOLD + userService.getOnlineUsers().size());
                board.add("Mode: " + CC.GOLD + gameService.getFormattedMode());
                board.add("Host: " + CC.GOLD + gameService.getFormattedHost());
                if (gameService.isScheduledToAutoStart()) {
                    board.add();
                    board.add(CC.WHITE + "Starting in:");
                    board.add(CC.YELLOW + TimeUtil.getNormalDuration(gameService.getAutoStartTask().getCountdown()));
                }
                board.add("");

                if (user.inState(UHCUserState.PRACTICE)) {
                    GameInfo gameInfo = user.getGameInfo();

                    board.add(CC.BOLD_PINK + "Practice " + CC.GRAY + '(' + userService.getPracticeUsers().size() + '/' + NumberOption.PRACTICE_SLOTS.getValue() + ')');
                    board.add(' ' + CC.GRAY + Lang.BULLET + ' ' + CC.WHITE + "Kills: " + CC.YELLOW + gameInfo.getPracticeKills().getValue());

                    if (gameInfo.getPracticeStreak().getValue() > 0) {
                        board.add(' ' + CC.GRAY + Lang.BULLET + ' ' + CC.WHITE + "Streak: " + CC.YELLOW + gameInfo.getPracticeStreak().getValue());
                    }
                    board.add(' ' + CC.GRAY + Lang.BULLET + ' ' + CC.WHITE + "Deaths: " + CC.YELLOW + gameInfo.getPracticeDeaths().getValue());

                } else {
                    board.add(CC.WHITE + "Scenarios:");

                    String scenarioPrefix = ' ' + CC.GRAY + Lang.BULLET + ' ';
                    if (enabledScenarios.isEmpty()) {
                        board.add(scenarioPrefix + CC.YELLOW + "Vanilla");
                    } else {
                        for (int i = 0; i < shownScenarios.size(); i++) {
                            StringBuilder builder = new StringBuilder();
                            builder.append(scenarioPrefix).append(CC.YELLOW).append(shownScenarios.get(i).getName());

                            if (i == shownScenarios.size() - 1 && enabledScenarios.size() > shownScenarios.size()) {
                                builder.append(' ').append(CC.GRAY).append("(+").append(enabledScenarios.size() - shownScenarios.size()).append(')');
                            }
                            board.add(builder.toString());
                        }
                    }
                }
                break;
            case SCATTERING:
                int countdown = scatterService.getTask().getCountdown();

                board.add("Starting in:");
                board.add(CC.YELLOW + countdown + " second" + (countdown == 1 ? "" : 's'));
                board.add("");
                board.add("Scattering: " + CC.GOLD + userService.getWaitingUsers().size());
                board.add("Scattered: " + CC.GOLD + userService.getAllScatterUsers().size());
                break;
            case PLAYING:
                board.add("Game Time: " + CC.GOLD + TimeUtil.getClockTime(System.currentTimeMillis() - gameService.getStartTime()));
                board.add("Players: " + CC.GOLD + userService.values()
                        .stream()
                        .filter(UHCUser::isPlaying)
                        .count() + '/' + gameService.getInitialPlayers());
                if (user.isPlaying()) {
                    board.add("Kills: " + CC.GOLD + user.getGameInfo().getKills().getValue());

                    if (gameService.isTeamGame()) {
                        board.add("Team Kills: " + CC.GOLD + user.getTeam().getTotalKills());
                    }
                }
                String formattedShrinkIn = borderService.getFormattedShrinkIn();
                board.add("Border: " + CC.GOLD + borderService.getCurrentRadius().getValue()
                        + (formattedShrinkIn == null ? "" : CC.GRAY + " (" + CC.RED + formattedShrinkIn + CC.GRAY + ')'));
                if (user.isPlaying()) {
                    NoCleanInfo noCleanInfo = user.getNoCleanInfo();
                    DoNotDisturbInfo doNotDisturbInfo = user.getTeam().getDoNotDisturbInfo();
                    if (noCleanInfo.isActive()) {
                        board.add("No Clean: " + CC.GOLD + noCleanInfo.getShortDuration());
                    } else if (doNotDisturbInfo.isActive()) {
                        board.add("DnD: " + CC.GOLD + doNotDisturbInfo.getShortDuration());
                    }
                }
                break;
            case ENDING:
                board.add("Finished In: " + CC.GOLD + TimeUtil.getClockTime(gameService.getEndTime() - gameService.getStartTime()));
                board.add("Participants: " + CC.GOLD + gameService.getInitialPlayers());
                if (user.isPlaying()) {
                    board.add("Your Kills: " + CC.GOLD + user.getGameInfo().getKills().getValue());

                    if (gameService.isTeamGame()) {
                        board.add("Team Kills: " + CC.GOLD + user.getTeam().getTotalKills());
                    }
                }
        }
        board.add("");
        board.add(CC.PURPLE + Lang.WEBSITE);
        board.add(CC.RED + CC.STRIKETHROUGH_GRAY + "--------------------");
        board.update();
    }

    @Override
    public void loadObjectives(UHCUser user, Scoreboard scoreboard) {
        Objective objective = scoreboard.getObjective("h");

        if (gameService.isStateOrHigher(GameState.PLAYING) || user.isInPractice()) {
            if (objective != null) return;

            objective = scoreboard.registerNewObjective("h", "health");
            objective.setDisplayName(CC.DARK_RED + Lang.HEART);
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        } else if (objective != null) {
            objective.unregister();
        }
    }
}
