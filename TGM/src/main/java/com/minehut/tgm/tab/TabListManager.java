package com.minehut.tgm.tab;

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

public class TabListManager implements Listener {
    @Getter
    private final List<PlayerList> playerLists = new ArrayList<>();

    public TabListManager() {
        TGM.registerEvents(this);
    }

    @EventHandler
    public void onMatchJoin(MatchJoinEvent event) {


        refreshAllTabs();
    }

    /**
     * Stops vanilla tab items from displaying.
     * Only our custom tab items should be displayed.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        PlayerList playerList = new PlayerList(event.getPlayer(), PlayerList.SIZE_FOUR);
        playerLists.add(playerList);
        playerList.initTable();

        for (Player other : Bukkit.getOnlinePlayers()) {
            PlayerList otherPlayerList = getPlayerList(other);

            //remove the joining player from everyone else's tab list.
            otherPlayerList.removePlayer(event.getPlayer());

            //remove everyone from the joining players tab.
            playerList.removePlayer(other);
        }

        refreshAllTabs();
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        refreshAllTabs();
    }

    public void refreshPlayerTab(PlayerContext playerContext) {
        PlayerList playerList = getPlayerList(playerContext.getPlayer());
        playerList.updateSlot(0,"Top Left tab");
        playerList.updateSlot(79,"Top Right tab");
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
