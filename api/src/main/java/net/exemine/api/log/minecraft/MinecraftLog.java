package net.exemine.api.log.minecraft;

import lombok.Getter;
import net.exemine.api.log.Log;
import net.exemine.api.log.LogType;

import java.util.UUID;

@Getter
public class MinecraftLog extends Log {

    private final UUID uuid;
    private final String instance;
    private final MinecraftLogType minecraftLogType;

    public MinecraftLog(MinecraftLogType minecraftLogType, String message, UUID uuid, String instance) {
        super(LogType.MINECRAFT, message);
        this.uuid = uuid;
        this.instance = instance;
        this.minecraftLogType = minecraftLogType;
    }
}
