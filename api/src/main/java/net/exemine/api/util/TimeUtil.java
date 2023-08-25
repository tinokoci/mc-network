package net.exemine.api.util;

import net.exemine.api.controller.ApiController;
import net.exemine.api.util.string.CC;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class TimeUtil {

    public static final long SECOND = 1000L;
    public static final long MINUTE = SECOND * 60;
    public static final long HOUR = MINUTE * 60;
    public static final long DAY = HOUR * 24;
    public static final long WEEK = DAY * 7;
    public static final long MONTH = DAY * 30;

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");
    public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");
    public static final String DUMMY_DATE = "20/01/2023 15:00";

    private static final SimpleDateFormat TIME_FORMAT =  createDateFormat("HH:mm");
    private static final SimpleDateFormat DATE_FORMAT = createDateFormat("dd/MM/yyyy HH:mm");
    private static final SimpleDateFormat FULL_DATE_FORMAT = createDateFormat("dd/MM/yyyy HH:mm:ss");

    private static SimpleDateFormat createDateFormat(String pattern) {
        return new SimpleDateFormat(pattern) {{
            setTimeZone(TIME_ZONE);
        }};
    }

    public static String getFullDuration(long millis) {
        StringBuilder builder = new StringBuilder();

        long days = millis / 86400000L;
        millis -= days * 86400000L;
        long hours = millis / 3600000L;
        millis -= hours * 3600000L;
        long minutes = millis / 60000L;
        millis -= minutes * 60000L;
        long seconds = millis / 1000L;

        if (days > 0) {
            builder.append(days).append(" day").append(days == 1 ? "" : "s");
        }
        if (hours > 0) {
            if (days > 0) {
                builder.append(minutes > 0 ? ", " : " and ");
            }
            builder.append(hours).append(" hour").append(hours == 1 ? "" : "s");
            if (minutes > 0 || seconds > 0) {
                builder.append(" ");
            }
        }
        if (minutes > 0) {
            if (hours > 0) builder.append("and ");
            builder.append(minutes).append(" minute").append(minutes == 1 ? "" : "s");
        }
        if (minutes == 0 && hours == 0 && days == 0) {
            builder.append(seconds).append(" second").append(seconds == 1 ? "" : "s");
        }
        return builder.toString();
    }

    public static String getPlayTime(long playtimeInMillis) {
        return playtimeInMillis == 0L
                ? "Unavailable"
                : getFullDuration(playtimeInMillis);
    }

    public static String getNormalDuration(long millis, String numberColor, String textColor) {
        if (millis == Long.MAX_VALUE) return "Permanent";
        if (numberColor == null) numberColor = "";
        if (textColor == null) textColor = "";

        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        long weeks = days / 7L;
        long months = weeks / 4L;
        long years = months / 12L;

        if (years > 0) {
            return numberColor + years + textColor + " year" + StringUtil.getPlural(years);
        } else if (months > 0) {
            return numberColor + months + textColor + " month" + StringUtil.getPlural(months);
        } else if (weeks > 0) {
            return numberColor + weeks + textColor + " week" + StringUtil.getPlural(weeks);
        } else if (days > 0) {
            return numberColor + days + textColor + " day" + StringUtil.getPlural(days);
        } else if (hours > 0) {
            return numberColor + hours + textColor + " hour" + StringUtil.getPlural(hours);
        } else if (minutes > 0) {
            return numberColor + minutes + textColor + " minute" + StringUtil.getPlural(minutes);
        } else {
            return numberColor + seconds + textColor + " second" + StringUtil.getPlural(seconds);
        }
    }

    public static String getNormalDuration(long millis) {
        return getNormalDuration(millis, null, null);
    }

    public static String getNormalDuration(int seconds, String numberColor, String textColor) {
        return getNormalDuration(seconds * 1000L, numberColor, textColor);
    }

    public static String getNormalDuration(int seconds) {
        return getNormalDuration(seconds, null, null);
    }

    public static String getShortDuration(long millis) {
        double seconds = ((double) millis / 1000L);
        double minutes = seconds / 60;
        double hours = minutes / 60;
        double days = hours / 24;

        if (seconds < 60) {
            return DECIMAL_FORMAT.format(seconds) + 's';
        }
        if (minutes < 60) {
            return minutes + "m";
        }
        if (hours < 24) {
            return hours + "h";
        }
        return days + "d";
    }

    public static String getShortDuration(int seconds) {
        return getShortDuration(seconds * 1000L);
    }

    public static String getDurationForExpirable(boolean permanent, long duration) {
        if (ApiController.getInstance().isMinecraftPlatform()) {
            return permanent ? CC.GREEN + "Permanent" : CC.RED + getNormalDuration(duration);
        }
        return permanent ? "Permanent" : getNormalDuration(duration);
    }

    public static String getCharDuration(long millis) {
        if (millis == Long.MAX_VALUE) return "Permanent";

        millis += 1L;

        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        long weeks = days / 7L;
        long months = weeks / 4L;
        long years = months / 12L;

        if (years > 0) return years + "y";
        else if (months > 0) return (months * 30) + "d";
        else if (weeks > 0) return weeks + "w";
        else if (days > 0) return days + "d";
        else if (hours > 0) return hours + "h";
        else if (minutes > 0) return minutes + "m";
        else return seconds + "s";
    }

    public static String getCharDuration(int seconds) {
        return getCharDuration(seconds * 1000L);
    }

    public static String getClockTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) ((millis / (1000 * 60)) % 60);
        int hours = (int) ((millis / (1000 * 60 * 60)) % 24);

        return (hours > 0 ? String.format("%02d", hours) + ":" : "")
                + String.format("%02d", minutes)
                + ":"
                + String.format("%02d", seconds);
    }

    public static String getClockTime(int seconds) {
        return getClockTime(seconds * 1000L);
    }

    public static long getMillisFromInput(String input) {
        input = input.toLowerCase();
        long result = 0L;

        StringBuilder builder = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                builder.append(c);
                continue;
            }
            String number = builder.toString();

            if (Character.isLetter(c) && !number.isEmpty()) {
                long value = convert(Integer.parseInt(number), c);

                if (value == 0L) {
                    continue;
                }
                result += value;
                builder.setLength(0);
            }
        }
        if (result == 0L) {
            return Long.MAX_VALUE;
        }
        return result;
    }

    private static long convert(int value, char charType) {
        switch (charType) {
            case 'y':
                return value * TimeUnit.DAYS.toMillis(365L);
            case 'M':
                return value * TimeUnit.DAYS.toMillis(30L);
            case 'w':
                return value * TimeUnit.DAYS.toMillis(7L);
            case 'd':
                return value * TimeUnit.DAYS.toMillis(1L);
            case 'h':
                return value * TimeUnit.HOURS.toMillis(1L);
            case 'm':
                return value * TimeUnit.MINUTES.toMillis(1L);
            case 's':
                return value * TimeUnit.SECONDS.toMillis(1L);
            default:
                return 0;
        }
    }

    public static String getTime(long millis) {
        return TIME_FORMAT.format(millis) + ' ' + TIME_ZONE.getID();
    }

    public static String getDate(long millis) {
        return DATE_FORMAT.format(millis) + ' ' + TIME_ZONE.getID();
    }

    public static String getFullDate(long millis) {
        return FULL_DATE_FORMAT.format(millis) + ' ' + TIME_ZONE.getID();
    }

    public static String getDate(LocalDateTime localDateTime) {
        return getDate(localDateTime.toEpochSecond(ZoneOffset.UTC) * 1000L);
    }

    public static boolean isDate(String value) {
        try {
            Date date = DATE_FORMAT.parse(value);
            return value.equals(DATE_FORMAT.format(date));
        } catch (Exception ex) {
            return false;
        }
    }

    public static long getMillisFromDate(String date) {
        if (!isDate(date)) return -1;
        try {
            return DATE_FORMAT.parse(date).getTime();
        } catch (ParseException e) {
            return -1;
        }
    }

    public static Period getCurrentPeriod() {
        LocalDateTime dateTime = LocalDateTime.now();
        return Period.of(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth());
    }

    public static boolean isInTimeSpan(int timeSpan, long value) {
        Calendar currentCalendar = Calendar.getInstance(TIME_ZONE);
        int currentTimeSpan = currentCalendar.get(timeSpan);
        int year = currentCalendar.get(Calendar.YEAR);

        Calendar targetCalendar = Calendar.getInstance(TIME_ZONE);
        targetCalendar.setTimeInMillis(value);
        int targetTimeSpan = targetCalendar.get(timeSpan);
        int targetYear = targetCalendar.get(Calendar.YEAR);

        return currentTimeSpan == targetTimeSpan && year == targetYear;
    }

    public static boolean isInTimeSpan(long value, long inclusiveStartTimestamp, long inclusiveEndTimestamp) {
        return value >= inclusiveStartTimestamp && value <= inclusiveEndTimestamp;
    }

    public static boolean shouldAlert(int countdown, int... excludedValues) {
        if (Arrays.stream(excludedValues).anyMatch(excludedValue -> excludedValue == countdown)) return false;

        Supplier<IntStream> alertTimesSupplier = () -> IntStream.of(1, 2, 3, 4, 5, 10, 15, 30, 45, 60);

        // If seconds then e.g. check if 15 seconds == 15 seconds
        // If minutes then e.g. check if 15 * 60 seconds == 15 minutes
        return ((countdown <= 60 && alertTimesSupplier.get().anyMatch(value -> value == countdown))
                || alertTimesSupplier.get().anyMatch(value -> value * 60 == countdown));
    }

    public static TimeZone getTimeZone(String timeZoneId) {
        return TimeZone.getTimeZone(Arrays.stream(TimeZone.getAvailableIDs())
                .filter(id -> id.equals(timeZoneId))
                .findFirst()
                .orElse("UTC"));
    }
}
