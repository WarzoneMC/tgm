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
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;

public class ChatModule extends MatchModule implements Listener {
    @Getter private TeamManagerModule teamManagerModule;
    @Getter private TimeModule timeModule;
    @Getter private final List<Chat> chatLog = new ArrayList<>();

    @Override
    public void load(Match match) {
        teamManagerModule = match.getModule(TeamManagerModule.class);
        timeModule = match.getModule(TimeModule.class);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncPlayerChatEvent event) {
        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(event.getPlayer());
        MatchTeam matchTeam = teamManagerModule.getTeam(event.getPlayer());
        event.setFormat(playerContext.getLevelString() + " " + (event.getPlayer().isOp() ? ChatColor.translateAlternateColorCodes('&', "&d&lOP ") : "") + matchTeam.getColor() + event.getPlayer().getName() + ChatColor.WHITE + ": " + event.getMessage().replaceAll("%", "%%"));
        chatLog.add(new Chat(playerContext.getUserProfile().getId().toString(), event.getPlayer().getName(), playerContext.getPlayer().getUniqueId().toString(), event.getMessage(), matchTeam.getId(), timeModule.getTimeElapsed(), false));
    }

    /*
    * Fired after the onChat so other plugins can
    * edit the chat format if needed, using the
    * AsyncPlayerChatEvent#setFormat() method.
    */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChatHighPriority(AsyncPlayerChatEvent event) {
        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(event.getPlayer());
        if (!event.isCancelled()) Bukkit.getOnlinePlayers().stream().forEach(player -> {
            TextComponent message = new TextComponent(event.getFormat().replaceAll("%%", "%"));
            BaseComponent[] stats = new BaseComponent[]{new TextComponent(ChatColor.AQUA + "Level: " + playerContext.getLevelString().replace("[", "").replace("]", "")),
                    new TextComponent("\n"),
                    new TextComponent("\n" + ChatColor.AQUA + "Kills: " + ChatColor.RESET + playerContext.getUserProfile().getKills()),
                    new TextComponent("\n" + ChatColor.AQUA + "Deaths: " + ChatColor.RESET + playerContext.getUserProfile().getDeaths()),
                    new TextComponent("\n" + ChatColor.AQUA + "K/D: " + ChatColor.RESET + playerContext.getUserProfile().getKDR()),
                    new TextComponent("\n"),
                    new TextComponent("\n" + ChatColor.AQUA + "Wins: " + ChatColor.RESET + playerContext.getUserProfile().getWins()),
                    new TextComponent("\n" + ChatColor.AQUA + "Losses: " + ChatColor.RESET + playerContext.getUserProfile().getLosses()),
                    new TextComponent("\n" + ChatColor.AQUA + "W/L: " + ChatColor.RESET + playerContext.getUserProfile().getWLR())};
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, stats));
            player.spigot().sendMessage(message);
        });
        event.setCancelled(true);
    }

    public void sendTeamChat(PlayerContext playerContext, String message) {
        MatchTeam matchTeam = teamManagerModule.getTeam(playerContext.getPlayer());
        for (PlayerContext member : matchTeam.getMembers()) {
            member.getPlayer().sendMessage(matchTeam.getColor() + "[" + matchTeam.getAlias() + "] "
                    + playerContext.getPlayer().getName() + ChatColor.WHITE + ": " + message);
        }
        chatLog.add(new Chat(playerContext.getUserProfile().getId().toString(), playerContext.getPlayer().getName(), playerContext.getPlayer().getUniqueId().toString(), message, matchTeam.getId(), timeModule.getTimeElapsed(), true));
    }

    @Override
    public void unload() {
        chatLog.clear();
    }
}
