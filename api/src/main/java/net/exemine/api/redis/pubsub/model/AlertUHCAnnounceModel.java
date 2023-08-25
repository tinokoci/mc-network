package net.exemine.api.redis.pubsub.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class AlertUHCAnnounceModel {

    private final String description;
    private final String clickable;
    private final String hover;
    private final String action;
}
