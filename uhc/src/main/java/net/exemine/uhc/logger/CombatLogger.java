package net.exemine.uhc.logger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class CombatLogger {

    private final UUID uuid;
    private final CombatLoggerEntity entity;
    private final long timestamp;
}
