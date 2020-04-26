package network.warzone.tgm.modules.reports;

import network.warzone.tgm.match.MatchModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReportsModule extends MatchModule implements Listener {

    private static final HashMap<String, Integer> amounts = new HashMap<>(); // String is reported's uuid, Integer is how many times the user has been reported
    private static final HashMap<String, Long> cooldown = new HashMap<>(); // String is reporter's uuid, Long is timestamp of latest report
    private static final List<Report> reports = new ArrayList<>(); // ArrayList with reports

    public static int getAmount(String uuid) {
        return amounts.getOrDefault(uuid, 0);
    }

    public static void setAmount(String uuid, int amount) {
        amounts.put(uuid, amount);
    }

    public static boolean cooldown(String uuid) {
        if (cooldown.containsKey(uuid)) {
            return System.currentTimeMillis() - cooldown.get(uuid) <= 30 * 1000; // 30 seconds cooldown
        }

        return false;
    }

    public static void setCooldown(String uuid, Long timestamp) {
        cooldown.put(uuid, timestamp);
    }

    public static void addReport(Report report) {
        reports.add(report);
    }

    public static List<Report> getReports() {
        return reports;
    }

    public static void clear() {
        amounts.clear();
        cooldown.clear();
        reports.clear();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        amounts.remove(event.getPlayer().getUniqueId().toString());
    }
}
