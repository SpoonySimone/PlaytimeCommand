package me.spoony.playtimecommand.utils;

public class FormatPlaytime {
    public static String formatPlaytime(int ticks) {
        int totalSeconds = ticks / 20;

        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append(days == 1 ? " day" : " days");
            if (hours > 0 || minutes > 0 || seconds > 0) {
                result.append(", ");
            }
        }

        if (hours > 0) {
            result.append(hours).append(hours == 1 ? " hour" : " hours");
            if (minutes > 0 || seconds > 0) {
                result.append(", ");
            }
        }

        if (minutes > 0) {
            result.append(minutes).append(minutes == 1 ? " minute" : " minutes");
            if (seconds > 0) {
                result.append(" and ");
            }
        }

        if (seconds > 0 || (days == 0 && hours == 0 && minutes == 0)) {
            result.append(seconds).append(seconds == 1 ? " second" : " seconds");
        }

        result.append(".");

        return result.toString();
    }
}
