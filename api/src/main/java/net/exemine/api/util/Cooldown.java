package net.exemine.api.util;

import java.util.HashMap;
import java.util.Map;

public class Cooldown<T> {

    private final Map<T, Long> cooldown = new HashMap<>();

    public boolean isActive(T key) {
        return System.currentTimeMillis() < cooldown.getOrDefault(key, 0L);
    }

    public boolean contains(T key) {
        return cooldown.containsKey(key);
    }

    public void put(T key, int seconds) {
        cooldown.put(key, System.currentTimeMillis() + seconds * 1000L);
    }

    public void remove(T key) {
        cooldown.remove(key);
    }

    public String getNormalDuration(T key) {
        if (!isActive(key)) return "now";
        return TimeUtil.getNormalDuration(cooldown.get(key) - System.currentTimeMillis());
    }

    public String getShortDuration(T key) {
        if (!isActive(key)) return "now";
        return TimeUtil.getShortDuration(cooldown.get(key) - System.currentTimeMillis());
    }
}
