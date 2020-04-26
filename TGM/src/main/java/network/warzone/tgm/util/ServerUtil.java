package network.warzone.tgm.util;

import java.text.NumberFormat;

/**
 * Created by Jorge on 10/05/2019
 */
public class ServerUtil {

    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public static long getUsedMemory() {
        return getTotalMemory() - getFreeMemory();
    }

    public static String getFormattedFreeMemory() {
        return formatMemory(getFreeMemory());
    }

    public static String getFormattedTotalMemory() {
        return formatMemory(getTotalMemory());
    }

    public static String getFormattedUsedMemory() {
        return formatMemory(getUsedMemory());
    }

    public static String formatMemory(long mem) {
        NumberFormat format = NumberFormat.getInstance();
        return format.format(mem / Math.pow(1024, 2)) + " MB";
    }
}
