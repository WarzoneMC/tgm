package network.warzone.tgm.modules;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.MatchResultEvent;
import network.warzone.tgm.modules.killstreak.KillstreakModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MatchResultModule extends MatchModule implements Listener {

    @EventHandler
    public void onMatchResult(MatchResultEvent event) {
        MatchTeam spectators = TGM.get().getModule(TeamManagerModule.class).getSpectators();

        for (Player player : Bukkit.getOnlinePlayers()) {
            int killstreak = TGM.get().getModule(KillstreakModule.class).getKillstreak(player.getUniqueId().toString());

            if (killstreak >= 5) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYour killstreak of &4" + killstreak + "&c ended."));
            }

            Location location = player.getLocation().clone().add(0.0, 100.0, 0.0);

            if (spectators.containsPlayer(player)) {
                player.playSound(location, Sound.ENTITY_WITHER_DEATH, 1000, 1);
            } else {
                if (event.getWinningTeam() == null) {
                    player.sendTitle("", ChatColor.YELLOW + "The result was a tie!", 10, 40, 10);
                    player.playSound(location, Sound.ENTITY_WITHER_SPAWN, 1000, 1);
                } else if (event.getWinningTeam().containsPlayer(player)) {
                    player.sendTitle("", ChatColor.GREEN + "Your team won!", 10, 40, 10);
                    player.playSound(location, Sound.ENTITY_WITHER_DEATH, 1000, 1);
                } else {
                    player.sendTitle("", ChatColor.RED + "Your team lost!", 10, 40, 10);
                    player.playSound(location, Sound.ENTITY_WITHER_SPAWN, 1000, 1);
                }
            }

            player.sendMessage("" + ChatColor.AQUA + ChatColor.STRIKETHROUGH + "---------------------");
            if (event.getWinningTeam() != null) {
                player.sendMessage(ChatColor.DARK_PURPLE + "  Winner: " + event.getWinningTeam().getColor() + event.getWinningTeam().getAlias());
            } else {
                player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.YELLOW + "  Tie!" + ChatColor.YELLOW + "");
            }
            if (event.getWinningTeam() != null && event.getWinningTeam().containsPlayer(player)) {
                player.sendMessage(ChatColor.GRAY + "  Congratulations!");
            }  else if (TGM.get().getModule(TeamManagerModule.class).getTeam(player) != null && TGM.get().getModule(TeamManagerModule.class).getTeam(player).isSpectator()) {
                player.sendMessage(ChatColor.GRAY + "  Play next game?");
            } else {
                player.sendMessage(ChatColor.GRAY+ "  Better luck next time!");
            }
                player.sendMessage("" + ChatColor.AQUA + ChatColor.STRIKETHROUGH + "---------------------");
        }
    }
}
