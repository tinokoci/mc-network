package net.exemine.api.leaderboard;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.exemine.api.data.stat.number.impl.IntTimedStat;

import java.util.UUID;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class RatioLeaderboardData implements Ranking {

    @Getter(AccessLevel.NONE)
    private final UUID uuid;
    private final String name;
    private final String displayName;
    private final IntTimedStat statA;
    private final IntTimedStat statB;

    private String formattedValue;
    private int placing;

    @Override
    public UUID getUniqueId() {
        return uuid;
    }
}


