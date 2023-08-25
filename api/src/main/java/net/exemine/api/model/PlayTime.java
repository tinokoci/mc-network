package net.exemine.api.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import net.exemine.api.data.stat.TimedStatSpan;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.DatabaseUtil;

import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class PlayTime {

    @SerializedName(DatabaseUtil.PRIMARY_KEY)
    private final String id;
    private final UUID uuid;
    private final Period periodTimestamp;
    private final long millisTimestamp;

    private Map<InstanceType, Long> values = new HashMap<>();

    public PlayTime(UUID uuid) {
        this.id = StringUtil.randomID();
        this.uuid = uuid;
        this.periodTimestamp = TimeUtil.getCurrentPeriod();
        this.millisTimestamp = System.currentTimeMillis();
    }

    public long getTotal() {
        return getInTimeSpan(TimedStatSpan.GLOBAL);
    }

    public long getTotal(InstanceType type) {
        if (type == null) return getTotal();
        return values.getOrDefault(type, 0L);
    }

    public long getInTimeSpan(TimedStatSpan timedStatSpan) {
        return getInTimeSpan(timedStatSpan, null);
    }

    public long getInTimeSpan(TimedStatSpan timedStatSpan, InstanceType type) {
        return values.entrySet()
                .stream()
                .filter(entry -> type == null || entry.getKey() == type)
                .filter(entry -> timedStatSpan.getTimeSpan() == -1
                        || TimeUtil.isInTimeSpan(timedStatSpan.getTimeSpan(), millisTimestamp))
                .mapToLong(Map.Entry::getValue)
                .sum();
    }

    public void increase(InstanceType type, long millis) {
        Long previousValue = values.get(type);

        if (previousValue != null) {
            millis += previousValue;
        }
        values.put(type, millis);
    }
}
