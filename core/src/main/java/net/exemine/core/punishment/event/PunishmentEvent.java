package net.exemine.core.punishment.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.exemine.api.punishment.Punishment;
import net.exemine.core.util.Event;

@AllArgsConstructor
@Getter
public class PunishmentEvent extends Event {

    private final Punishment punishment;
}