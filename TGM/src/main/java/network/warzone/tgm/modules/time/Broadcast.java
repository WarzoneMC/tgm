package network.warzone.tgm.modules.time;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorge on 10/21/2017.
 */

@AllArgsConstructor
public class Broadcast {

    @Getter private BroadcastTimeType timeType;
    @Getter private String message;
    @Getter int interval;
    @Getter private List<Integer> excludedTimes;

    public void run(int time) {
        if (timeType == null || time == 0) return;
        if (timeType.equals(BroadcastTimeType.ABSOLUTE)) {
            if (time == interval) {
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message).replace("%time%", String.valueOf(time)).replace("%time_formatted%", Strings.formatTime(time)));
            }
        } else if (timeType.equals(BroadcastTimeType.MULTIPLE)) {
            if (time % interval == 0 && !excludedTimes.contains(time)) {
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message).replace("%time%", String.valueOf(time)).replace("%time_formatted%", Strings.formatTime(time)));
            }
        }
    }

}
