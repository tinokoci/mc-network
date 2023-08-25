package net.exemine.uhc.game.sync;

import net.exemine.api.match.impl.uhc.UHCMatch;
import net.exemine.api.redis.RedisService;
import net.exemine.api.redis.pubsub.RedisMessage;
import net.exemine.api.util.Executor;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.util.MessageUtil;
import net.exemine.uhc.UHC;
import net.exemine.uhc.autostart.AutoStartTask;
import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.config.option.ToggleOption;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.scenario.Scenario;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import net.exemine.uhc.user.UHCUserState;

import java.util.Arrays;
import java.util.UUID;

public class CalendarSetupTask {

    private final UHC plugin;
    private final UHCUserService userService;
    private final RedisService redisService;

    public CalendarSetupTask(UHC plugin) {
        this.plugin = plugin;
        this.userService = plugin.getUserService();
        this.redisService = plugin.getCore().getRedisService();
        subscribeToUHCSetup();
    }

    private void subscribeToUHCSetup() {
        redisService.subscribe(RedisMessage.UHC_SETUP, UHCMatch.class, match -> {
            GameService gameService = plugin.getGameService();
            if (!gameService.isUseMatchSynchronization()
                    || gameService.isMatchSynchronized()
                    || gameService.isNotState(GameState.LOBBY)) return;

            // Copy The Match First
            gameService.setMatch(match);

            // Configuration And Scenarios
            ToggleOption.NETHER.update(match.isNether());
            NumberOption.PLAYERS_PER_TEAM.setValue(getTeamSize(match.getMode()));
            Arrays.stream(Scenario.values()).forEach(scenario -> {
                boolean enabled = match.getScenarios().contains(scenario.getName());
                scenario.update(enabled);
            });
            // Setup Host And Supervisor
            setAssignee(match.getHostUuid(), UHCUserState.HOST);
            if (match.hasSupervisor()) {
                setAssignee(match.getSupervisorUuid(), UHCUserState.SUPERVISOR);
            }
            // Schedule The Game And Finish
            int autoStartDurationInMinutes = 10;
            gameService.setAutoStartTask(new AutoStartTask(plugin, match.getStartTime() + TimeUtil.MINUTE * autoStartDurationInMinutes));
            MessageUtil.send(CC.BOLD_GREEN + "This game has been synchronized with a match from the upcoming games calendar.");
        });
    }

    private void setAssignee(UUID uuid, UHCUserState state) {
        GameService gameService = plugin.getGameService();

        if (state != UHCUserState.HOST && state != UHCUserState.SUPERVISOR) {
            throw new IllegalArgumentException("You can only set assignees for HOST and SUPERVISOR positions");
        }
        UHCUserState disabledState = gameService.isStateOrHigher(GameState.SCATTERING) ? UHCUserState.SPECTATOR : UHCUserState.LOBBY;

        userService.fetch(uuid).ifPresentOrElse(assignee -> {
            UHCUser currentAssignee = state == UHCUserState.HOST
                    ? gameService.getHost()
                    : gameService.getSupervisor();
            if (currentAssignee != null && currentAssignee != assignee) {
                currentAssignee.setState(disabledState);
                if (currentAssignee.isOnline()) {
                    currentAssignee.sendMessage(assignee.getColoredRealName() + CC.RED + " has forcefully replaced you as the game " + state.name().toLowerCase() + ".");
                }
            }
            Executor.schedule(() -> assignee.setState(state)).runSync();
        }, () -> MessageUtil.send(CC.RED + "The provided " + state.name().toLowerCase() + " doesn't exist in the system!"));
    }

    private int getTeamSize(String mode) {
        if (mode.equalsIgnoreCase("FFA")) return 1;
        return Integer.parseInt(mode.replace("To", ""));
    }
}
