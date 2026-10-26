package vetclinic.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * ValidationUtil - small helper methods that the setters use to check input.
 * Owner: JP
 *
 * OOP concepts: STATIC methods (you call them on the class, no object needed) and a PRIVATE
 * constructor, so nobody can create a ValidationUtil object by mistake.
 */
public final class ValidationUtil {

    // Utility class: all methods are static, so we block "new ValidationUtil()".
    private ValidationUtil() {
    }

    // True if the text is null, empty, or only spaces.
    public static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    // Philippine mobile format used by our project: 11 digits, starts with 09.
    public static boolean isValidContactNumber(String number) {
        if (number == null) {
            return false;
        }
        String text = number.trim();
        if (text.length() != 11 || !text.startsWith("09")) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // True if min <= value <= max.
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    // True if the date is after today.
    public static boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    // Reads a date typed as yyyy-MM-dd (for example 2026-10-07).
    public static LocalDate parseDate(String text) {
        if (isBlank(text)) {
            throw new IllegalArgumentException("Date must not be empty. Use yyyy-MM-dd, for example 2026-10-07.");
        }
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Wrong date format. Use yyyy-MM-dd, for example 2026-10-07.");
        }
    }
}
