package com.minehut.tgm.modules;

import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import com.sk89q.minecraft.util.commands.ChatColor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatModule extends MatchModule {
    @Getter private TeamManagerModule teamManagerModule;

    @Override
    public void load(Match match) {
        teamManagerModule = match.getModule(TeamManagerModule.class);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        MatchTeam matchTeam = teamManagerModule.getTeam(event.getPlayer());
        event.setFormat("<" + matchTeam.getColor() + event.getPlayer().getName() + ChatColor.WHITE + "> " + event.getMessage());
    }

    public void sendTeamChat(PlayerContext playerContext, String message) {
        MatchTeam matchTeam = teamManagerModule.getTeam(playerContext.getPlayer());
        for (PlayerContext member : matchTeam.getMembers()) {
            member.getPlayer().sendMessage(matchTeam.getColor() + "[" + matchTeam.getAlias() + "] "
                    + playerContext.getPlayer().getName() + ChatColor.WHITE + ": " + message);
        }
    }
}
