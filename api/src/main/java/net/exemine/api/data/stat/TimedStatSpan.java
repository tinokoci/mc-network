package net.exemine.api.data.stat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Calendar;

@RequiredArgsConstructor
@Getter
public enum TimedStatSpan {

    GLOBAL("Global", -1),
    MONTHLY("Monthly", Calendar.MONTH),
    WEEKLY("Weekly", Calendar.WEEK_OF_YEAR);

    private final String name;
    private final int timeSpan;
}
