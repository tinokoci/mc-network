package net.exemine.api.log;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Log {

    private final LogType logType;
    private final String message;
    private final long timestamp = System.currentTimeMillis();
}