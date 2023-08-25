package net.exemine.api.redis.pubsub.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.punishment.PunishmentType;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class PunishmentExecutionModel {

    @Getter(AccessLevel.NONE)
    private final UUID uuid;
    private final PunishmentType type;
    private final int index;

    public UUID getUniqueId() {
        return uuid;
    }
}
