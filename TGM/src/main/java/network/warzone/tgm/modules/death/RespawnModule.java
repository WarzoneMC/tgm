package network.warzone.tgm.modules.death;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import network.warzone.tgm.player.event.TGMPlayerRespawnEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RespawnModule extends MatchModule implements Listener {

    private List<Player> spectators;
    private Map<UUID, Float> spectatorTime;
    private TeamManagerModule teamManagerModule;

    private BukkitTask task;

    public void load(Match match) {
        spectators = new ArrayList<>();
        teamManagerModule = match.getModule(TeamManagerModule.class);
        spectatorTime = new HashMap<>();
    }

    public void enable() {
        task = Bukkit.getScheduler().runTaskTimer(TGM.get(), this::updateTitle, 0, 2L);
    }

    public void disable() {
        task.cancel();
    }

    @EventHandler
    public void onPlayerDeath(TGMPlayerDeathEvent event) {
        Player victim = event.getVictim();
        MatchTeam matchTeam = teamManagerModule.getTeam(victim);

        if (matchTeam.isSpectator()) return;

        startSpectating(event.getVictim(), matchTeam.getRespawnDelay(), event);

        Bukkit.getScheduler().runTaskLater(TGM.get(), () -> stopSpectating(event.getVictim()), matchTeam.getRespawnDelay() * 20);
    }

    private void updateTitle() {
        for (Player spectator : spectators) {
            float timeLeft = (spectatorTime.get(spectator.getUniqueId()));
            spectator.sendTitle(
                    ChatColor.RED.toString() + ChatColor.BOLD.toString() + "YOU DIED",
                    ChatColor.GRAY + "Respawning in " + ChatColor.YELLOW + String.format("%.1f", timeLeft) + 's',
                    0, 3, 0);
            spectatorTime.replace(spectator.getUniqueId(), (float) (timeLeft - 0.1));
        }
    }

    private void startSpectating(Player player, int respawnDelay, TGMPlayerDeathEvent event) {
        spectators.add(player);
        spectatorTime.put(player.getUniqueId(), (float) respawnDelay);

        player.sendTitle(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "YOU DIED", "", 0, 3, 0);

        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            if (event.getKiller() != null) {
                player.teleport(event.getKiller().getLocation(), PlayerTeleportEvent.TeleportCause.SPECTATE);
            } else {
                player.teleport(teamManagerModule.getSpectators().getSpawnPoints().get(0).getLocation(), PlayerTeleportEvent.TeleportCause.SPECTATE);
            }
        }

        player.setAllowFlight(true);
        player.setFlying(true);
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            player.setVelocity(player.getVelocity().setY(0.95));
        } else {
            player.setVelocity(player.getVelocity().setY(0));
        }
        player.setGameMode(GameMode.SPECTATOR);
    }

    private void stopSpectating(Player player) {
        spectators.remove(player);
        spectatorTime.remove(player.getUniqueId());

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 999, 1);
        player.setAllowFlight(false);
        player.setFlying(false);

        Bukkit.getPluginManager().callEvent(new TGMPlayerRespawnEvent(player));
    }

}