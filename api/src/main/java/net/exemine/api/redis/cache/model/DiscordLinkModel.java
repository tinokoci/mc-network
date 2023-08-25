package net.exemine.api.redis.cache.model;

import lombok.Getter;

@Getter
public class DiscordLinkModel {

    private final String userId;
    private final String userName;
    private final long timestamp;

    public DiscordLinkModel(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        this.timestamp = System.currentTimeMillis();
    }
}
