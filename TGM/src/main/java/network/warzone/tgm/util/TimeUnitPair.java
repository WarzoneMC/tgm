package network.warzone.tgm.util;

import lombok.Getter;

import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;

/**
 * Created by Jorge on 08/24/2019
 */
@Getter
public class TimeUnitPair {

    private int value;
    private ChronoUnit unit;

    private String toString;

    public TimeUnitPair(int value, ChronoUnit unit) {
        this.value = value;
        this.unit = unit;
    }

    public String toString() {
        if (toString == null) {
            if (this.unit == ChronoUnit.FOREVER || toMilliseconds() == -1) toString = "permanent";
            else if (value == 1) {
                if (this.unit == ChronoUnit.MILLENNIA) toString = value + " millennium";
                else if (this.unit == ChronoUnit.CENTURIES) toString = value + " century";
                else if (this.unit.name().toLowerCase().endsWith("s")) {
                    toString = value + " " + this.unit.name().substring(0, this.unit.name().length() - 1).toLowerCase().replace("_", " ");
                }
                else toString = value + " " + this.unit.name().toLowerCase().replace("_", " ");
            } else toString = value + " " + this.unit.name().toLowerCase().replace("_", " ");
        }
        return toString;
    }

    public long toMilliseconds() {
        if (this.unit == ChronoUnit.FOREVER) {
            return -1;
        }
        if (value <= 0) return -1;
        return this.unit.getDuration().getSeconds() * value * 1000;
    }

    public static TimeUnitPair permanent() {
        return new TimeUnitPair(1, ChronoUnit.FOREVER);
    }

    private static ChronoUnit getChronoUnit(String s) {
        for (ChronoUnit unit : ChronoUnit.values()) {
            if (unit == ChronoUnit.NANOS || unit == ChronoUnit.MICROS || unit == ChronoUnit.MILLIS) continue;
            if (unit.name().toLowerCase().startsWith(s.toLowerCase())) {
                return unit;
            }
        }
        return null;
    }

    public static TimeUnitPair parse(String s) {
        if ("permanent".equalsIgnoreCase(s) ||
                "perm".equalsIgnoreCase(s) ||
                "p".equalsIgnoreCase(s) ||
                "forever".equalsIgnoreCase(s) ||
                "f".equalsIgnoreCase(s) ||
                "-1".equalsIgnoreCase(s)) return new TimeUnitPair(1, ChronoUnit.FOREVER);
        ChronoUnit timeUnit;

        StringBuilder time = new StringBuilder();
        String unit = "";
        boolean digitsDone = false;
        for (int i = 0; i < s.length(); i++) {
            if (!digitsDone && Character.isDigit(s.charAt(i))) {
                time.append(s.charAt(i));
            } else if (!Character.isDigit(s.charAt(i))) {
                digitsDone = true;
                unit += s.charAt(i);
            } else {
                break;
            }
        }
        timeUnit = TimeUnitPair.getChronoUnit(unit);
        if (timeUnit == null) return null;
        return new TimeUnitPair(Integer.parseInt(time.toString()), timeUnit);
    }

    public static String formatToSeconds(double ticks) {
        DecimalFormat df = new DecimalFormat("0.0");
        double ticksPerSecond = 20;
        double d = (ticks / ticksPerSecond);

        return df.format(d);
    }

}
