package network.warzone.tgm.modules.reports;

import network.warzone.tgm.match.MatchModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

public class ReportsModule extends MatchModule implements Listener {

    private static final HashMap<String, Integer> amount = new HashMap<>(); // String is user's uuid, Integer is how many times the user has been reported
    private static final HashMap<String, Long> cooldown = new HashMap<>(); // String is user's uuid, Long is timestamp of user's latest report

    public static int getAmount(String uuid) {
        if (amount.containsKey(uuid)) {
            amount.put(uuid, amount.get(uuid) + 1);
            return amount.get(uuid);
        } else {
            amount.put(uuid, 1);
            return 1;
        }
    }

    public static boolean cooldown(String uuid) {
        if (cooldown.containsKey(uuid)) {
            if (System.currentTimeMillis() - cooldown.get(uuid) > 30 * 1000) { // 30 seconds cooldown
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public static void setCooldown(String uuid) {
        cooldown.put(uuid, System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        amount.remove(uuid);
    }
}
