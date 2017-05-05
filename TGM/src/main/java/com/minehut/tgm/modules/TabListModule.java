package com.minehut.tgm.modules;

import com.minehut.tgm.TGM;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.match.MatchStatus;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.util.Strings;
import com.minehut.tgm.util.TitleAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TabListModule extends MatchModule implements Listener {

    @Getter private String header;

    @Getter protected int runnableId = -1;

    @Override
    public void load(Match match) {
        //todo: show all authors.
        header = ChatColor.GRAY + " - " + ChatColor.WHITE + ChatColor.BOLD.toString() + "TEAM.GG" + ChatColor.GRAY + " - ";
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        refreshTab(event.getPlayer());
    }

    @Override
    public void enable() {
        refreshAllTabs();

        runnableId = Bukkit.getScheduler().scheduleSyncRepeatingTask(TGM.get(), new Runnable() {
            @Override
            public void run() {
                refreshAllTabs();
            }
        }, 20L, 20L);
    }

    private void refreshTab(Player player) {
        MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();

        ChatColor timeColor = ChatColor.GREEN;
        if (matchStatus == MatchStatus.PRE) {
            timeColor = ChatColor.GOLD;
        } else if (matchStatus == MatchStatus.POST) {
            timeColor = ChatColor.RED;
        }

        String footer =
                ChatColor.GRAY + "Time: " + timeColor + Strings.formatTime(TGM.get().getMatchManager().getMatch().getModule(TimeModule.class).getTimeElapsed())
                + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Spectators: " + ChatColor.AQUA + TGM.get().getModule(TeamManagerModule.class).getSpectators().getMembers().size();
        TitleAPI.sendTabTitle(player, header, footer);
    }
    private void refreshAllTabs() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            refreshTab(player);
        }
    }
}
