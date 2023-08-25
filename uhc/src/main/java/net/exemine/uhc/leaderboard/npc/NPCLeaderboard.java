package net.exemine.uhc.leaderboard.npc;

import lombok.Data;
import net.exemine.core.nms.npc.NPC;

@Data
public class NPCLeaderboard {

    private final NPC npc;
    private final String username;
    private final int placing;
}

