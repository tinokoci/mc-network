package net.exemine.uhc.practice.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.core.util.Event;
import net.exemine.uhc.user.UHCUser;

@RequiredArgsConstructor
@Getter
public class PracticeKillStreakEvent extends Event {

    private final UHCUser user;
    private final int value;
}
