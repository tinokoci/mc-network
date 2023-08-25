package net.exemine.uhc.game.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.core.util.Event;
import net.exemine.uhc.team.Team;

@RequiredArgsConstructor
@Getter
public class GameEndEvent extends Event {

    private final Team winningTeam;
}
