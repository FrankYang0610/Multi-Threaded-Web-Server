package com.frankyang.polyu.comp2322.webserver.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * <h3>The {@code DateTimeManager} class</h3>
 * An important utility class, mainly used to output the correct time to the log.
 * <p>
 * Worth noticing that the caller can customize this class.
 * The caller can choose whether to output the days of the week and timezone information in the log.
 * <p>
 * The default time output format is: {@code EEE, dd MMM yyyy HH:mm:ss 'GMT'}.
 * For example, {@code Tue, 15 Apr 2025 18:05:22 GMT}.
 */
public class DateTimeManager {
    public static final String RFC_7231_PATTERN = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
    public static final String NO_TIME_ZONE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss";

    public static final ZoneId GMT_ZONE = ZoneId.of("GMT");

    public static final DateTimeFormatter HTTP_DATE_FORMATTER = DateTimeFormatter
            .ofPattern(RFC_7231_PATTERN)
            .withLocale(Locale.US)
            .withZone(GMT_ZONE);

    /**
     * @return the current system time
     */
    public static String getDateTime() {
        return HTTP_DATE_FORMATTER.format(Instant.now());
    }

    public static String formatHttpDate(long timestamp) {
        return HTTP_DATE_FORMATTER.format(Instant.ofEpochMilli(timestamp));
    }

    public static long parseHttpDate(String httpDate) {
        try {
            return ZonedDateTime.parse(httpDate, HTTP_DATE_FORMATTER)
                    .toInstant()
                    .toEpochMilli();
        } catch (Exception e) {
            return 0;
        }
    }
}
