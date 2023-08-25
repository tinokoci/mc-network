package net.exemine.api.redis.pubsub.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class RankUpdateModel {

    @Getter(AccessLevel.NONE)
    private final UUID uuid;
    private final boolean mainRankChanged;

    public UUID getUniqueId() {
        return uuid;
    }
}
