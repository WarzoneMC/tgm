package com.minehut.tgm.modules;

import com.minehut.teamapi.models.Chat;
import com.minehut.tgm.TGM;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import com.sk89q.minecraft.util.commands.ChatColor;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;

public class ChatModule extends MatchModule implements Listener {
    @Getter private TeamManagerModule teamManagerModule;
    @Getter private TimeModule timeModule;
    @Getter
    private final List<Chat> chatLog = new ArrayList<>();

    @Override
    public void load(Match match) {
        teamManagerModule = match.getModule(TeamManagerModule.class);
        timeModule = match.getModule(TimeModule.class);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(event.getPlayer());
        MatchTeam matchTeam = teamManagerModule.getTeam(event.getPlayer());
        event.setFormat("<" + matchTeam.getColor() + event.getPlayer().getName() + ChatColor.WHITE + "> " + event.getMessage());

        chatLog.add(new Chat(playerContext.getUserProfile().getId(), event.getPlayer().getName(), event.getMessage(), matchTeam.getId(), (int) timeModule.getTimeElapsed(), false));
    }

    public void sendTeamChat(PlayerContext playerContext, String message) {
        MatchTeam matchTeam = teamManagerModule.getTeam(playerContext.getPlayer());
        for (PlayerContext member : matchTeam.getMembers()) {
            member.getPlayer().sendMessage(matchTeam.getColor() + "[" + matchTeam.getAlias() + "] "
                    + playerContext.getPlayer().getName() + ChatColor.WHITE + ": " + message);
        }
        chatLog.add(new Chat(playerContext.getUserProfile().getId(), playerContext.getPlayer().getName(), message, matchTeam.getId(), (int) timeModule.getTimeElapsed(), true));
    }

    @Override
    public void unload() {
        chatLog.clear();
    }
}
