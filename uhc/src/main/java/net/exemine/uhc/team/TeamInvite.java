package net.exemine.uhc.team;

import lombok.Data;
import net.exemine.uhc.user.UHCUser;

@Data
public class TeamInvite {

    private final UHCUser inviter;
    private final Team team;
    private int taskId;

    public boolean hasInviterChangedTeams() {
        return inviter.getTeam() != team;
    }
}
