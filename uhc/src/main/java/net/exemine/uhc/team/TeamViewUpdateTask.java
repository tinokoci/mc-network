package net.exemine.uhc.team;

import net.exemine.core.lunar.impl.LunarTeamView;
import net.exemine.uhc.UHC;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class TeamViewUpdateTask extends BukkitRunnable {

    private final UHC uhc;

    public TeamViewUpdateTask(UHC uhc) {
        this.uhc = uhc;

        runTaskTimer(uhc, 0L, 10L);
    }

    @Override
    public void run() {
        if (!uhc.getGameService().isTeamGame()) return;

        for (Team team : uhc.getTeamService().getAliveTeams()) {
            for (UHCUser member : team.getAliveMembers()) {
                new LunarTeamView(team.getLeader().getUniqueId(), new ArrayList<>(team.getAliveMembers()))
                        .send(member);
            }
        }
    }
}
