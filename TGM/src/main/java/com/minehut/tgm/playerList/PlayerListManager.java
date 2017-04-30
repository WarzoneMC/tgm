package com.minehut.tgm.playerList;

import com.minehut.tgm.TGM;
import com.minehut.tgm.join.MatchJoinEvent;
import com.minehut.tgm.team.TeamChangeEvent;
import com.minehut.tgm.user.PlayerContext;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerListManager implements Listener {
    @Getter
    private final List<PlayerList> playerLists = new ArrayList<>();

    @Getter private PlayerListController playerListController;

    public PlayerListManager() {
        this.playerListController = new PlayerListControllerImpl(this);

        TGM.registerEvents(this);
    }

    @EventHandler
    public void onMatchJoin(MatchJoinEvent event) {
        refreshAllTabs();
    }

    /**
     * Stops vanilla playerList items from displaying.
     * Only our custom playerList items should be displayed.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        PlayerList playerList = new PlayerList(event.getPlayer(), PlayerList.SIZE_FOUR);
        playerLists.add(playerList);

        Bukkit.getScheduler().scheduleSyncDelayedTask(TGM.getTgm(), new Runnable() {
            @Override
            public void run() {
                for (Player other : Bukkit.getOnlinePlayers()) {
                    PlayerList otherPlayerList = getPlayerList(other);

                    //remove the joining player from everyone else's playerList list.
                    otherPlayerList.removePlayer(event.getPlayer());

                    //remove everyone from the joining players playerList.
                    playerList.removePlayer(other);
                }

                playerList.initTable(playerListController.getBlankTexture());
                refreshAllTabs();
            }
        }, 0L);
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        refreshAllTabs();
    }

    public void refreshPlayerTab(PlayerContext playerContext) {
        playerListController.refreshView(playerContext);
    }

    public void refreshAllTabs() {
        for (PlayerContext playerContext : TGM.getPlayerManager().getPlayers()) {
            refreshPlayerTab(playerContext);
        }
    }

    public PlayerList getPlayerList(Player player) {
        for (PlayerList playerList : playerLists) {
            if (playerList.getPlayer() == player) {
                return playerList;
            }
        }
        return null;
    }
}
