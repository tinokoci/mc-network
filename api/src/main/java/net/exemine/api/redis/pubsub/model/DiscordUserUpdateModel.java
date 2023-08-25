package net.exemine.api.redis.pubsub.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.rank.Rank;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class DiscordUserUpdateModel {

    private final String userId;
    private final String nickname;
    private final List<Rank> ranks;
    private final boolean locked;
}
