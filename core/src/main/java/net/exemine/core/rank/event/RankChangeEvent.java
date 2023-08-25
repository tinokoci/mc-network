package net.exemine.core.rank.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.rank.Rank;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.Event;

@RequiredArgsConstructor
@Getter
public class RankChangeEvent extends Event {

    private final CoreUser user;
    private final Rank rank;
    private final boolean mainRankChanged;
}
