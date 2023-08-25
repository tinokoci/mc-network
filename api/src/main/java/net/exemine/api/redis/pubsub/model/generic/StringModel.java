package net.exemine.api.redis.pubsub.model.generic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class StringModel {

    private final String message;
}
