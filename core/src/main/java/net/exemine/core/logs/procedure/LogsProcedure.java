package net.exemine.core.logs.procedure;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.log.minecraft.MinecraftLogType;
import net.exemine.api.util.TimeUtil;
import net.exemine.core.user.base.ExeUser;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class LogsProcedure {

    private static final Map<UUID, LogsProcedure> PROCEDURES = new HashMap<>();

    private final ExeUser<?> user;
    private ExeUser<?> target;

    private LogsProcedureState state = LogsProcedureState.START_TIMESTAMP;
    private MinecraftLogType type = MinecraftLogType.PUBLIC;
    private long startTimestamp = System.currentTimeMillis() - TimeUtil.DAY;
    private long endTimestamp = System.currentTimeMillis();

    public LogsProcedure(ExeUser<?> user) {
        this.user = user;
        PROCEDURES.put(user.getUniqueId(), this);
    }

    public void cancel() {
        PROCEDURES.remove(user.getUniqueId());
    }

    public static LogsProcedure getProcedure(ExeUser<?> user) {
        return PROCEDURES.getOrDefault(user.getUniqueId(), null);
    }
}
