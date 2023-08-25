package net.exemine.uhc.scatter;

import lombok.Getter;
import net.exemine.api.util.Executor;
import net.exemine.api.util.string.CC;
import net.exemine.core.Core;
import net.exemine.core.util.MessageUtil;
import net.exemine.uhc.UHC;
import net.exemine.uhc.border.BorderService;
import net.exemine.uhc.config.option.ToggleOption;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.game.event.ScatterStartEvent;
import net.exemine.uhc.team.Team;
import net.exemine.uhc.team.TeamService;
import net.exemine.uhc.user.UHCUserService;
import net.exemine.uhc.user.UHCUserState;
import net.exemine.uhc.world.WorldService;

@Getter
public class ScatterService {

    private final UHC plugin;

    private final BorderService borderService;
    private final GameService gameService;
    private final TeamService teamService;
    private final UHCUserService userService;
    private final WorldService worldService;

    private ScatterTask task;
    private boolean scatterStarted;

    public ScatterService(UHC plugin) {
        this.plugin = plugin;
        this.borderService = plugin.getBorderService();
        this.gameService = plugin.getGameService();
        this.teamService = plugin.getTeamService();
        this.userService = plugin.getUserService();
        this.worldService = plugin.getWorldService();
    }

    public void startScatter(int countdown) {
        if (scatterStarted) return;
        scatterStarted = true;

        gameService.cancelAutoStart();
        //userService.getPracticeUsers().forEach(practiceUser -> practiceUser.setState(UHCUserState.LOBBY));
        ToggleOption.PRACTICE.setEnabled(false);

        plugin.getServer().getPluginManager().callEvent(new ScatterStartEvent());

        Executor.schedule(() -> {
            MessageUtil.send(CC.BOLD_YELLOW + "The game is now starting...");
            Core.get().getServerService().setChatMuted(true);

            // Assign everyone who is not in a team
            userService.getWaitingUsers()
                    .stream()
                    .filter(user -> user.getTeam() == null)
                    .forEach(teamService::createTeam);

            // Teleport spectators to the UHC world
            userService.getSpectatorUsers().forEach(spectator -> {
                Team team = spectator.getTeam();
                // Kick the moderator from the team if he joined in one before modding himself
                if (team != null) {
                    teamService.removeFromTeam(spectator, team, true);
                }
                spectator.teleport(worldService.getUhcWorld().getSpawnLocation());
                spectator.setAllowFlight(true);
                spectator.setFlying(true);
            });
            task = new ScatterTask(plugin, countdown);
            gameService.setState(GameState.SCATTERING);
        }).runSyncLater(3000L);
    }
}
