package network.warzone.tgm.modules;

import network.warzone.tgm.TGM;
import network.warzone.tgm.map.MapRotationFile;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.MatchResultEvent;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.killstreak.KillstreakModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.player.event.PlayerJoinTeamAttemptEvent;
import network.warzone.tgm.player.event.TGMPlayerRespawnEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.ref.WeakReference;

public class MatchResultModule extends MatchModule implements Listener {

    private WeakReference<Match> match;
    private TeamManagerModule teamManagerModule;

    @Override
    public void load(Match match) {
        this.match = new WeakReference<Match>(match);
        this.teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRespawn(TGMPlayerRespawnEvent event) {
        if (TGM.get().getMatchManager().getMatch().getMatchStatus() != MatchStatus.POST) return;
        if (this.teamManagerModule.getTeam(event.getPlayer()).isSpectator()) return;
        applyPostPlayer(event.getPlayer());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
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
                applyPostPlayer(player);

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
        MapRotationFile rotation = TGM.get().getMatchManager().getMapRotation();
        rotation.saveRotationPosition(rotation.getCurrent() + 1);
    }

    private void applyPostPlayer(Player player) {
        if (TGM.get().getConfig().getBoolean("map.post-block-break", false) && player.hasPermission("tgm.post.break")) {
            player.setGameMode(GameMode.SURVIVAL);
        } else {
            player.setGameMode(GameMode.ADVENTURE);
        }
        player.setAllowFlight(true);
        player.setVelocity(player.getVelocity().setY(1.0)); // Weeee!
        player.setFlying(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 1000000, 5, true, false), true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMatchResult(PlayerJoinTeamAttemptEvent event) {
        if (match.get().getMatchStatus().equals(MatchStatus.POST)) {
            event.getPlayerContext().getPlayer().sendMessage(ChatColor.RED + "The match has already ended.");
            event.setCancelled(true);
        }
    }
}
