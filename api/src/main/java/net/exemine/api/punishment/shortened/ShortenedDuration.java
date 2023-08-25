package net.exemine.api.punishment.shortened;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.util.TimeUtil;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ShortenedDuration {

    PERMANENT(Long.MAX_VALUE),
    DAYS_60(TimeUtil.MONTH * 2),
    DAYS_30(TimeUtil.MONTH),
    DAYS_14(TimeUtil.WEEK * 2),
    DAYS_7(TimeUtil.DAY * 7);

    private final long duration;

    public static long shortenDuration(long duration) {
        return Arrays.stream(values())
                .map(ShortenedDuration::getDuration)
                .filter(valueDuration -> valueDuration < duration)
                .findFirst()
                .orElse(0L);
    }
}
