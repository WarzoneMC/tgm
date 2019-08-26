package network.warzone.tgm.util;

import lombok.Getter;

import java.time.temporal.ChronoUnit;

/**
 * Created by Jorge on 08/24/2019
 */
@Getter
public class TimeUnitPair {

    int value;
    ChronoUnit timeUnit;

    private final String toString;

    public TimeUnitPair(int value, ChronoUnit timeUnit) {
        this.value = value;
        this.timeUnit = timeUnit;
        if (timeUnit == ChronoUnit.FOREVER || toMilliseconds() == -1) toString = "permanent";
        else if (value == 1) {
            if (timeUnit == ChronoUnit.MILLENNIA) toString = value + " millennium";
            else if (timeUnit == ChronoUnit.CENTURIES) toString = value + " century";
            else if (timeUnit.name().toLowerCase().endsWith("s")) {
                toString = value + " " + timeUnit.name().substring(0, timeUnit.name().length() - 1).toLowerCase().replace("_", " ");
            }
            else toString = value + " " + timeUnit.name().toLowerCase().replace("_", " ");
        } else toString = value + " " + timeUnit.name().toLowerCase().replace("_", " ");
    }

    public String toString() {
        return toString;
    }

    public long toMilliseconds() {
        if (timeUnit == ChronoUnit.FOREVER) {
            return -1;
        }
        if (value <= 0) return -1;
        return timeUnit.getDuration().getSeconds() * value * 1000;
    }

    public static TimeUnitPair permanent() {
        return new TimeUnitPair(1, ChronoUnit.FOREVER);
    }

    public static ChronoUnit getChronoUnit(String s) {
        for (ChronoUnit timeUnit : ChronoUnit.values()) {
            if (timeUnit == ChronoUnit.NANOS || timeUnit == ChronoUnit.MICROS || timeUnit == ChronoUnit.MILLIS) continue;
            if (timeUnit.name().toLowerCase().startsWith(s.toLowerCase())) {
                return timeUnit;
            }
        }
        return null;
    }

    public static TimeUnitPair parse(String s) {
        if (s.equalsIgnoreCase("permanent") ||
                s.equalsIgnoreCase("perm") ||
                s.equalsIgnoreCase("p") ||
                s.equalsIgnoreCase("forever") ||
                s.equalsIgnoreCase("f") ||
                s.equalsIgnoreCase("-1")) return new TimeUnitPair(1, ChronoUnit.FOREVER);
        ChronoUnit timeUnit;

        String time = "";
        String unit = "";
        boolean digitsDone = false;
        for (int i = 0; i < s.length(); i++) {
            if (!digitsDone && Character.isDigit(s.charAt(i))) {
                time += s.charAt(i);
            } else if (!Character.isDigit(s.charAt(i))) {
                digitsDone = true;
                unit += s.charAt(i);
            } else {
                break;
            }
        }
        timeUnit = TimeUnitPair.getChronoUnit(unit);
        if (timeUnit == null) return null;
        return new TimeUnitPair(Integer.valueOf(time), timeUnit);
    }

}
