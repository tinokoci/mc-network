package net.exemine.api.data.stat.number.impl;

import lombok.RequiredArgsConstructor;
import net.exemine.api.data.stat.TimedStatSpan;
import net.exemine.api.data.stat.number.NumberTimedStat;
import net.exemine.api.util.TimeUtil;

import java.util.Map;

@RequiredArgsConstructor
public final class IntTimedStat extends NumberTimedStat<Integer> {

    public IntTimedStat(int initialValue) {
        add(initialValue);
    }

    @Override
    public Integer getTotal() {
        return map.values().stream().mapToInt(value -> value).sum();
    }

    @Override
    public Integer getWeekly() {
        return getInTimeSpan(TimedStatSpan.WEEKLY);
    }

    @Override
    public Integer getMonthly() {
        return getInTimeSpan(TimedStatSpan.MONTHLY);
    }

    @Override
    public void increment() {
        add(1);
    }

    @Override
    public Integer getInTimeSpan(TimedStatSpan timedStatSpan) {
        if (timedStatSpan == TimedStatSpan.GLOBAL) return getTotal();

        return map.entrySet()
                .stream()
                .filter(entry -> TimeUtil.isInTimeSpan(timedStatSpan.getTimeSpan(), entry.getKey()))
                .mapToInt(Map.Entry::getValue)
                .sum();
    }
}
