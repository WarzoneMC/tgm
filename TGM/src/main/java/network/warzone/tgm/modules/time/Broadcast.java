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

    @Getter private String message;
    @Getter private List<String> commands;
    @Getter int interval;
    @Getter boolean repeat;
    @Getter private List<Integer> exclude;

    public void run(int time) {
        if (time == 0) return;
        if (repeat && time % interval == 0 && !exclude.contains(time)) {
            dispatch(time);
        } else if (time == interval) {
            dispatch(time);
        }
    }

    private void dispatch(int time) {
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message).replace("%time%", String.valueOf(time)).replace("%time_formatted%", Strings.formatTime(time)));
        commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%time%", String.valueOf(time)).replace("%time_formatted%", Strings.formatTime(time))));
    }

}
