package net.exemine.api.redis.pubsub.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.proxy.ProxyCheckState;

@RequiredArgsConstructor
@Getter
public class ProxyCheckStateUpdateModel {

    private final String address;
    private final ProxyCheckState newState;
}
