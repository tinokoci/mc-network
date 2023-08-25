package net.exemine.core.rank.procedure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RankProcedureDuration {

    ONE_MINUTE("1 minute", "1m"),
    ONE_HOUR("1 hour", "1h"),
    ONE_DAY("1 day", "1d"),
    ONE_WEEK("1 week", "7d"),
    ONE_MONTH("1 month", "30d"),
    PERMANENT("Permanent", "perm");

    private final String name;
    private final String format;
}