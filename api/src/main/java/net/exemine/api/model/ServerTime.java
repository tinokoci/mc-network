package net.exemine.api.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ServerTime {

    DEFAULT("Default", 0L),
    DAY("Day", 6000L),
    SUNSET("Sunset",12000L),
    NIGHT("Night", 18000L);

    private final String name;
    private final long value;
}
