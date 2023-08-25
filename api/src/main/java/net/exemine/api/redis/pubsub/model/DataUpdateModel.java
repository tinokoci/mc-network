package net.exemine.api.redis.pubsub.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.data.ExeData;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class DataUpdateModel {

    private final Class<? extends ExeData> dataClazz;
    private final UUID uuid;
}
