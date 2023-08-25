package net.exemine.api.redis.pubsub.model.generic;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UUIDModel {

    private final UUID uuid;

    public UUID getUniqueId() {
        return uuid;
    }
}
