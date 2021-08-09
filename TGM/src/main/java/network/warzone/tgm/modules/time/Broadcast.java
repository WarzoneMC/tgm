package network.warzone.tgm.modules.time;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.List;

import static net.kyori.adventure.text.Component.text;

/**
 * Created by Jorge on 10/21/2017.
 */

@AllArgsConstructor @Getter
public class Broadcast {

    private String message;
    private List<String> commands;

    private int interval;
    private boolean repeat;
    private List<Integer> exclude;

    public void run(int time) {
        if (time == 0) return;
        if (repeat && time % interval == 0 && !exclude.contains(time)) {
            dispatch(time);
        } else if (time == interval) {
            dispatch(time);
        }
    }

    private void dispatch(int time) {
        Bukkit.broadcast(text(ChatColor.translateAlternateColorCodes('&', message).replace("%time%", String.valueOf(time)).replace("%time_formatted%", Strings.formatTime(time))));
        commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%time%", String.valueOf(time)).replace("%time_formatted%", Strings.formatTime(time))));
    }

}
