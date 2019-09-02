package network.warzone.tgm.player;

import lombok.Getter;
import network.warzone.tgm.user.PlayerContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by luke on 4/28/17.
 */
public class PlayerManager {

    @Getter Collection<PlayerContext> players = new ConcurrentLinkedQueue<>();

    public void addPlayer(PlayerContext playerContext) {
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
