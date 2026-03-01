package me.spoony.playtimecommand.utils;

import java.util.ArrayList;
import java.util.List;

public class FormatPlaytime {
    public static String formatPlaytime(int ticks) {
        int totalSeconds = ticks / 20;

        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        List<String> components = new ArrayList<>();

        if (days > 0) {
            components.add(days + (days == 1 ? " day" : " days"));
        }

        if (hours > 0) {
            components.add(hours + (hours == 1 ? " hour" : " hours"));
        }

        if (minutes > 0) {
            components.add(minutes + (minutes == 1 ? " minute" : " minutes"));
        }

        if (seconds > 0 || components.isEmpty()) {
            components.add(seconds + (seconds == 1 ? " second" : " seconds"));
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < components.size(); i++) {
            result.append(components.get(i));
            
            if (i < components.size() - 2) {
                result.append(", ");
            } else if (i == components.size() - 2) {
                result.append(" and ");
            }
        }

        result.append(".");

        return result.toString();
    }
}