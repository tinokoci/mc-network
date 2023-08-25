package net.exemine.api.redis.pubsub.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class InstanceCommandModel {

    private final String name;
    private final String command;
}
