package net.exemine.uhc.leaderboard.npc;

import lombok.RequiredArgsConstructor;
import net.exemine.api.util.Executor;
import net.exemine.core.nms.npc.NPCInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class NPCLeaderboardListener implements Listener {

    private final NPCLeaderboardService npcLeaderboardService;

    @EventHandler
    public void onNPCInteract(NPCInteractEvent event) {
        NPCLeaderboard leaderboard = npcLeaderboardService.getLeaderboard(event.getNpc().getEntityId());

        if (leaderboard != null) {
            Executor.schedule(() -> event.getPlayer().performCommand("stats " + leaderboard.getUsername())).runSync();
        }
    }
}

