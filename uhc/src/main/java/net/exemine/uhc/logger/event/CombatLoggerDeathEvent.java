package net.exemine.uhc.logger.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.core.util.Event;
import net.exemine.uhc.logger.CombatLoggerEntity;

@RequiredArgsConstructor
@Getter
public class CombatLoggerDeathEvent extends Event {

    private final CombatLoggerEntity combatLoggerEntity;
}
