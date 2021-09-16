package network.warzone.tgm.util;

import network.warzone.tgm.match.Match;
import network.warzone.tgm.modules.countdown.Countdown;
import network.warzone.tgm.modules.time.TimeModule;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Created by Jorge on 10/12/2019
 */
public class Placeholders {

    public static String apply(String input, Map<String, String> placeholders) {
        String output = input;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            output = output.replaceAll("%" + entry.getKey() + "%", entry.getValue());
        }
        return output;
    }

    public static void addPlaceholders(Map<String, String> placeholders, Player player) {
        placeholders.put("playerName", player.getName());
        placeholders.put("playerUuid", player.getUniqueId().toString());
        placeholders.put("playerX", String.valueOf(player.getLocation().getX()));
        placeholders.put("playerY", String.valueOf(player.getLocation().getY()));
        placeholders.put("playerZ", String.valueOf(player.getLocation().getZ()));
    }

    public static void addPlaceholders(Map<String, String> placeholders, Match match) {
        placeholders.put("timeElapsed", String.valueOf(match.getModule(TimeModule.class).getTimeElapsed()));
        placeholders.put("timeElapsedFormatted", Strings.formatTime(match.getModule(TimeModule.class).getTimeElapsed()));
        placeholders.put("mapName", match.getMapContainer().getMapInfo().getName());
        placeholders.put("mapGametype", match.getMapContainer().getMapInfo().getGametype().name());
        placeholders.put("mapGametypeName", match.getMapContainer().getMapInfo().getGametype().getName());
    }

    public static void addPlaceholders(Map<String, String> placeholders, Countdown countdown) {
        placeholders.put("countdownTime", String.valueOf(countdown.getTimeMaxSeconds()));
        placeholders.put("countdownTimeFormatted", Strings.formatTime(countdown.getTimeMaxSeconds()));
        placeholders.put("countdownTimeLeft", String.valueOf(countdown.getTimeLeftSeconds()));
        placeholders.put("countdownTimeLeftFormatted", Strings.formatTime(countdown.getTimeLeftSeconds()));
    }

}
