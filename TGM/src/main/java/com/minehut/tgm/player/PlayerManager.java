package com.minehut.tgm.player;

import com.minehut.tgm.user.PlayerContext;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luke on 4/28/17.
 */
public class PlayerManager {
    @Getter
    List<PlayerContext> players = new ArrayList<>();

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

    public void broadcastToAdmins(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                player.sendMessage(message);
            }
        }
    }
}
