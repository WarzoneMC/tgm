package network.warzone.tgm.clickevent;

import network.warzone.tgm.match.Match;
import network.warzone.tgm.util.Placeholders;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jorge on 10/11/2019
 */
public class RunAsPlayerClickEvent extends ClickEvent {

    private List<String> commands;

    public RunAsPlayerClickEvent(List<String> commands) {
        this.commands = commands;
    }

    @Override
    public void run(Match match, Player player) {
        Map<String, String> placeholders = new HashMap<>();
        Placeholders.addPlaceholders(placeholders, match);
        Placeholders.addPlaceholders(placeholders, player);
        commands.forEach(command -> Placeholders.apply(command, placeholders));
    }

}
