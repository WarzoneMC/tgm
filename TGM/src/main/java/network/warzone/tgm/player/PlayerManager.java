package network.warzone.tgm.player;

import lombok.Getter;
import network.warzone.tgm.user.PlayerContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Created by luke on 4/28/17.
 */
public class PlayerManager {

    @Getter private Collection<PlayerContext> players = new ConcurrentLinkedQueue<>();

    public void addPlayer(PlayerContext playerContext) {
        List<PlayerContext> toRemove = players.stream()
                .filter(p -> !Bukkit.getOnlinePlayers().contains(p.getPlayer()))
                .collect(Collectors.toList());
        this.players.removeAll(toRemove);
        this.players.add(playerContext);
    }

    public void removePlayer(PlayerContext playerContext) {
        this.players.remove(playerContext);
    }

    public PlayerContext getPlayerContext(Player player) {
        for (PlayerContext playerContext : players) {
            if (playerContext.getPlayer() == player) {
                return playerContext;
            }
        }
        return null;
    }

    public PlayerContext getPlayerContext(UUID uuid) {
        for (PlayerContext context : players) {
            if (context.getPlayer().getUniqueId().equals(uuid)) {
                return context;
            }
        }
        return null;
    }

    public PlayerContext getPlayerContext(String uuid) {
        for (PlayerContext context : players) {
            if (context.getPlayer().getUniqueId().toString().equals(uuid)) {
                return context;
            }
        }
        return null;
    }

    public void broadcastToAdmins(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                player.sendMessage(message);
            }
        }
        //if no players are online, Bukkit.broadcastMessage() doesn't log.
        if (Bukkit.getOnlinePlayers().size() == 0) {
            Bukkit.getLogger().severe(message);
        }
    }
}
