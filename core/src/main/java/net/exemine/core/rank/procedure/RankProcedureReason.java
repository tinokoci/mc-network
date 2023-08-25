package net.exemine.core.rank.procedure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RankProcedureReason {

    HIRED("Hired"),
    PROMOTED("Promoted"),
    PRIZE("Prize"),
    GIVEAWAY("Giveaway"),
    STORE_ISSUES("Store Issues"),
    OTHER("Other");

    private final String name;
}