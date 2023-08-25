package net.exemine.uhc.assign;

import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.util.MessageUtil;
import net.exemine.uhc.UHC;
import net.exemine.uhc.team.Team;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class AutoAssignTask extends BukkitRunnable {

    private final UHC plugin;

    private final int startValue;
    private int countdown;

    private final boolean firstAssign;

    public AutoAssignTask(UHC plugin, boolean firstAssign) {
        this.plugin = plugin;
        this.startValue = 60;
        this.countdown = startValue;
        this.firstAssign = firstAssign;
        plugin.getGameService().setPvP(false);
        plugin.getGameService().setAutoAssignRunning(true);
        runTaskTimerAsynchronously(plugin, 20L, 20L);
    }

    @Override
    public void run() {
        if (--countdown == 0) {
            cancel();

            boolean isTeamGame = plugin.getGameService().isTeamGame();
            List<Team> teams = plugin.getTeamService().getAliveTeams();

            MessageUtil.send("");
            MessageUtil.send(CC.BOLD_PINK + "Assigned " + (isTeamGame ? "Teams" : "Players"));

            for (int i = 0; i < teams.size(); i += 2) {
                Team teamA = teams.get(i);

                if (i < (teams.size() - 1)) {
                    Team teamB = teams.get(i + 1);

                    teamA.setAssignedTeam(teamB);
                    teamB.setAssignedTeam(teamA);

                    if (plugin.getGameService().isTeamGame()) {
                        MessageUtil.send(Lang.LIST_PREFIX + teamA.getLeader().getColoredDisplayName() + CC.GRAY + "'s team vs. " + teamB.getLeader().getColoredDisplayName() + CC.GRAY + "'s team");
                    } else {
                        MessageUtil.send(Lang.LIST_PREFIX + teamA.getLeader().getColoredDisplayName() + CC.GRAY + " vs. " + teamB.getLeader().getColoredDisplayName());
                    }
                }
            }
            MessageUtil.send("");
            plugin.getGameService().setPvP(true);
            return;
        }
        if (TimeUtil.shouldAlert(countdown, startValue)) {
            MessageUtil.send(CC.BOLD_GOLD + "[Assign] " + CC.GRAY + (firstAssign ? "First" : "Next") + " round is starting in " + CC.PINK + countdown + CC.GRAY + " second" + (countdown == 1 ? "" : "s") + '.', Sound.ORB_PICKUP);
        }
    }
}
