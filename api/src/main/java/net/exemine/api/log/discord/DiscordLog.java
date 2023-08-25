package net.exemine.api.log.discord;

import lombok.Getter;
import net.exemine.api.log.Log;
import net.exemine.api.log.LogType;

@Getter
public class DiscordLog extends Log {

    private final String userId;
    private final String channelName;

    public DiscordLog(LogType logType, String message, String userId, String channelName) {
        super(logType, message);
        this.userId = userId;
        this.channelName = channelName;
    }
}
