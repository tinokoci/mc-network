package net.exemine.uhc.user.info;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.exemine.api.util.Cooldown;
import net.exemine.uhc.team.Team;

@RequiredArgsConstructor
@Getter
@Setter
public class DoNotDisturbInfo {

    public static final Cooldown<DoNotDisturbInfo> COOLDOWN = new Cooldown<>();

    private final Team team;
    private Team enemy;

    public boolean isActive() {
        return COOLDOWN.isActive(this);
    }

    public String getShortDuration() {
        return COOLDOWN.getShortDuration(this);
    }
}
