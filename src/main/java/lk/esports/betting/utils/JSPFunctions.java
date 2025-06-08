package lk.esports.betting.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for JSP functions to handle LocalDateTime formatting
 */
public class JSPFunctions {

    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return "";
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return dateTime.format(formatter);
        } catch (Exception e) {
            return dateTime.toString();
        }
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return formatDateTime(dateTime, "MMM dd, yyyy HH:mm");
    }

    public static String formatDate(LocalDateTime dateTime) {
        return formatDateTime(dateTime, "MMM dd, yyyy");
    }

    public static String formatTime(LocalDateTime dateTime) {
        return formatDateTime(dateTime, "HH:mm");
    }

    public static String formatShort(LocalDateTime dateTime) {
        return formatDateTime(dateTime, "MMM dd, HH:mm");
    }
}