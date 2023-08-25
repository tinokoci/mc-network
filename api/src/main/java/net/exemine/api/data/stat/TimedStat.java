package net.exemine.api.data.stat;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TimedStat<T> {

    protected Map<Long, T> map = new HashMap<>();

    public void put(long timestamp, T value) {
        map.put(timestamp, value);
    }

    public void add(T value) {
        map.put(System.currentTimeMillis(), value);
    }
}
