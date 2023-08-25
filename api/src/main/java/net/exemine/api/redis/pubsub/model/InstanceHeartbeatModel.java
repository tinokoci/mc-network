package net.exemine.api.redis.pubsub.model;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.rank.Rank;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class InstanceHeartbeatModel {

    private final String name;
    private final InstanceType type;
    private final List<String> playerNames;
    private final int onlinePlayers;
    private final int maxPlayers;
    private final double tps1;
    private final double tps2;
    private final double tps3;
    private final Rank whitelistRank;
    private final JsonObject extra;
}
