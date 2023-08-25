package net.exemine.uhc.leaderboard.npc;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.data.stat.TimedStatSpan;
import net.exemine.api.leaderboard.LeaderboardData;
import net.exemine.api.leaderboard.LeaderboardService;
import net.exemine.api.util.string.CC;
import net.exemine.core.nms.hologram.Hologram;
import net.exemine.uhc.UHC;
import org.bukkit.scheduler.BukkitRunnable;

public class NPCLeaderboardTask extends BukkitRunnable {

    private final LeaderboardService<UHCData> leaderboardService;
    private final NPCLeaderboardService npcLeaderboardService;

    public NPCLeaderboardTask(UHC plugin, LeaderboardService<UHCData> leaderboardService, NPCLeaderboardService npcLeaderboardService) {
        this.leaderboardService = leaderboardService;
        this.npcLeaderboardService = npcLeaderboardService;
        runTaskTimerAsynchronously(plugin, 0L, 20L);
    }

    @Override
    public void run() {
        npcLeaderboardService.getNpcLeaderboards().values().forEach(npcLeaderboard -> {
            try {
                Hologram value = npcLeaderboard.getNpc().getAttachedHologram().getLineBelow(0);
                LeaderboardData data = leaderboardService.getByKey(npcLeaderboardService.getKey(), TimedStatSpan.GLOBAL).get(npcLeaderboard.getPlacing() - 1);
                value.rename(data.getDisplayName() + CC.GRAY + " - " + CC.WHITE + data.getStat().getInTimeSpan(TimedStatSpan.GLOBAL));
            } catch (Exception ignored) {} // shouldn't happen, but I don't want error spam whatsoever
        });
    }
}

