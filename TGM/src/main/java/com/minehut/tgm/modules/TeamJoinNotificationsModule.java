package com.minehut.tgm.modules;

import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.team.TeamJoinEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TeamJoinNotificationsModule extends MatchModule implements Listener {

    @EventHandler
    public void onTeamJoin(TeamJoinEvent event) {
        Bukkit.broadcastMessage("join event!");
        event.getPlayerContext().getPlayer().sendMessage(ChatColor.WHITE + "You joined " + event.getTeam().getColor() + ChatColor.BOLD + event.getTeam().getAlias());
    }
}
