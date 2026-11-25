package util;

import exception.InvalidTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Helper methods for formatting, parsing, and validating parking timestamps.
 */
public final class DateTimeHelper {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(ParkingConstants.DATE_TIME_PATTERN);

    private DateTimeHelper() {
    }

    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(FORMATTER);
    }

    public static LocalDateTime parse(String text) throws InvalidTimeException {
        if (text == null || text.trim().isEmpty()) {
            throw new InvalidTimeException("Timestamp cannot be empty.");
        }
        try {
            return LocalDateTime.parse(text.trim(), FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidTimeException(
                    "Invalid timestamp format '" + text + "'. Expected " + ParkingConstants.DATE_TIME_PATTERN, e);
        }
    }

    /**
     * Computes elapsed minutes between entry and exit, rejecting null or reversed times.
     */
    public static long calculateDurationMinutes(LocalDateTime entryTime, LocalDateTime exitTime)
            throws InvalidTimeException {
        if (entryTime == null || exitTime == null) {
            throw new InvalidTimeException("Entry time and exit time must both be present.");
        }
        if (exitTime.isBefore(entryTime)) {
            throw new InvalidTimeException("Exit time cannot be earlier than entry time.");
        }
        return ChronoUnit.MINUTES.between(entryTime, exitTime);
    }
}
