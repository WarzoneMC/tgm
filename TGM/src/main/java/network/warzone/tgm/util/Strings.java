package network.warzone.tgm.util;

import network.warzone.warzoneapi.models.Author;

public class Strings {
    public static String formatTime(double time) {
        boolean negative = false;
        if (time < 0) {
            negative = true;
            time *= -1;
        }
        int hours = (int) time / 3600;
        int minutes = (int) (time - (hours * 3600)) / 60;
        int seconds = (int) time - (hours * 3600) - (minutes * 60);
        String hoursString = hours + "";
        String minutesString = minutes + "";
        String secondsString = seconds + "";
        while (minutesString.length() < 2) {
            minutesString = "0" + minutesString;
        }
        while (secondsString.length() < 2) {
            secondsString = "0" + secondsString;
        }
        return (negative ? "-" : "") + (hours == 0 ? "" : hoursString + ":") + minutesString + ":" + secondsString;
    }

    public static String getTechnicalName(String s) {
        return s.toUpperCase().replace(" ", "_");
    }

    public static String getAgo(long timestamp) {
        double time = (System.currentTimeMillis() - timestamp) / 1000D;

        int t;
        if ((t = (int) Math.floor(time / 31536000)) > 0) {
            return (t == 1) ? "1 year" : t + " years";
        } else if ((t = (int) Math.floor(time / 2592000)) > 0) {
            return (t == 1) ? "1 month" : t + " months";
        } else if ((t = (int) Math.floor(time / 604800)) > 0) {
            return (t == 1) ? "1 week" : t + " weeks";
        } else if ((t = (int) Math.floor(time / 86400)) > 0) {
            return (t == 1) ? "1 day" : t + " days";
        } else if ((t = (int) Math.floor(time / 3600)) > 0) {
            return (t == 1) ? "1 hour" : t + " hours";
        } else if ((t = (int) Math.floor(time / 60)) > 0) {
            return (t == 1) ? "1 minute" : t + " minutes";
        } else if ((t = (int) Math.floor(time)) > 0) {
            return (t == 1) ? "1 second" : t + " seconds";
        } else {
            return "moments";
        }
    }

    public static String getAuthorUsername(Author author) {
        return author.getDisplayUsername() != null ? author.getDisplayUsername() : author.getUsername() != null ? author.getUsername() : "Unknown";
    }

    public static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

}
