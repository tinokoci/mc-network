package net.exemine.api.redis.pubsub.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.rank.Rank;

@RequiredArgsConstructor
@Getter
public class AlertStaffMessageModel {

    private final Rank rank;
    private final String message;
}
