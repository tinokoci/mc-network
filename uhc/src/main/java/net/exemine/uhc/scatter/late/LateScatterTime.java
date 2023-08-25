package net.exemine.uhc.scatter.late;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.TimeUtil;

import java.util.Arrays;
import java.util.Comparator;

@RequiredArgsConstructor
@Getter
public enum LateScatterTime {

    DONATOR(Rank.PRIME, TimeUtil.MINUTE * 20),
    DEFAULT(Rank.DEFAULT, TimeUtil.MINUTE * 10);

    private final Rank rank;
    private final long time;

    public static long get(Rank rank) {
        return Arrays.stream(values())
                .filter(time -> rank.isEqualOrAbove(time.getRank()))
                .min(Comparator.comparing(time -> time.getRank().getPriority()))
                .orElse(DEFAULT)
                .getTime();
    }
}
