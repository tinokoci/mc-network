package net.exemine.api.data.stat.number.impl;

import lombok.RequiredArgsConstructor;
import net.exemine.api.data.stat.TimedStatSpan;
import net.exemine.api.data.stat.number.NumberTimedStat;
import net.exemine.api.util.TimeUtil;

import java.util.Map;

@RequiredArgsConstructor
public final class DoubleTimedStat extends NumberTimedStat<Double> {

    public DoubleTimedStat(double initialValue) {
        add(initialValue);
    }

    @Override
    public Double getTotal() {
        return map.values().stream().mapToDouble(value -> value).sum();
    }

    @Override
    public Double getWeekly() {
        return getInTimeSpan(TimedStatSpan.WEEKLY);
    }

    @Override
    public Double getMonthly() {
        return getInTimeSpan(TimedStatSpan.MONTHLY);
    }

    @Override
    public void increment() {
        add(1.0);
    }

    @Override
    public Double getInTimeSpan(TimedStatSpan timedStatSpan) {
        if (timedStatSpan == TimedStatSpan.GLOBAL) return getTotal();

        return map.entrySet()
                .stream()
                .filter(entry -> TimeUtil.isInTimeSpan(timedStatSpan.getTimeSpan(), entry.getKey()))
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }
}
