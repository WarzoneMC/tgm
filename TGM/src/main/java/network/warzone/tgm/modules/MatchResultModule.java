package network.warzone.tgm.modules;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.MatchResultEvent;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MatchResultModule extends MatchModule implements Listener {

    @EventHandler
    public void onMatchResult(MatchResultEvent event) {
        MatchTeam spectators = TGM.get().getModule(TeamManagerModule.class).getSpectators();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (spectators.containsPlayer(player)) {
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1f, 1f);
            } else {
                if (event.getWinningTeam() == null) {
                    player.sendTitle("", ChatColor.YELLOW + "The result was a tie!", 10, 40, 10);
                    player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 1f);
                } else if (event.getWinningTeam().containsPlayer(player)) {
                    player.sendTitle("", ChatColor.GREEN + "Your team won!", 10, 40, 10);
                    player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1f, 1f);
                } else {
                    player.sendTitle("", ChatColor.RED + "Your team lost!", 10, 40, 10);
                    player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 1f);
                }
            }

            player.sendMessage("" + ChatColor.AQUA + ChatColor.STRIKETHROUGH + "-----------------------------------");
            if (event.getWinningTeam() != null) {
                player.sendMessage(ChatColor.DARK_PURPLE + "               Winning Team: " + event.getWinningTeam().getColor() + event.getWinningTeam().getAlias());
            } else {
                player.sendMessage(ChatColor.DARK_PURPLE + "                             " + ChatColor.YELLOW + "Tie!" + ChatColor.YELLOW + "");
            }
            if (event.getWinningTeam().containsPlayer(player)) {
                player.sendMessage(ChatColor.GRAY + "                  Congratulations!");
            }  else if (TGM.get().getModule(TeamManagerModule.class).getTeam(player) != null && TGM.get().getModule(TeamManagerModule.class).getTeam(player).isSpectator()) {
                player.sendMessage(ChatColor.GRAY + "                   Play next game?");
            } else {
                player.sendMessage(ChatColor.GRAY+ "               Better luck next time!");
            }
                player.sendMessage("" + ChatColor.AQUA + ChatColor.STRIKETHROUGH + "-----------------------------------");
        }
    }
}
