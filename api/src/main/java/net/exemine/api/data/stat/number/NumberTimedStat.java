package net.exemine.api.data.stat.number;

import net.exemine.api.data.stat.TimedStat;
import net.exemine.api.data.stat.TimedStatSpan;

public abstract class NumberTimedStat<T extends Number> extends TimedStat<T> {

    public abstract T getTotal();

    public abstract T getWeekly();

    public abstract T getMonthly();

    public abstract void increment();

    public abstract T getInTimeSpan(TimedStatSpan timedStatSpan);
}
