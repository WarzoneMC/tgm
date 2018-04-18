package network.warzone.tgm.modules;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.time.TimeModule;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.warzoneapi.models.Chat;
import network.warzone.warzoneapi.models.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Getter
public class ChatModule extends MatchModule implements Listener {

    private TeamManagerModule teamManagerModule;
    private TimeModule timeModule;
    private final List<Chat> chatLog = new ArrayList<>();

    @Override
    public void load(Match match) {
        teamManagerModule = match.getModule(TeamManagerModule.class);
        timeModule = match.getModule(TimeModule.class);
    }

    private final List<String> blockedCmds = Arrays.asList("t ", "w ", "r ", "reply", "minecraft:w", "tell", "minecraft:tell", "minecraft:t ", "msg", "minecraft:msg");

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(event.getPlayer());
        if (playerContext.getUserProfile().getLatestMute() != null && playerContext.getUserProfile().getLatestMute().isActive() && startsWith(event.getMessage())) {
            Punishment punishment = playerContext.getUserProfile().getLatestMute();
            sendMutedMessage(event.getPlayer(), punishment);
            event.setCancelled(true);
        }
    }

    private boolean startsWith(String msg) {
        for (String cmd : blockedCmds) {
            if (msg.toLowerCase().startsWith("/" + cmd) || msg.toLowerCase().startsWith(cmd)) return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncPlayerChatEvent event) {
        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(event.getPlayer());
        if (playerContext.getUserProfile().getLatestMute() != null && playerContext.getUserProfile().getLatestMute().isActive()) {
            Punishment punishment = playerContext.getUserProfile().getLatestMute();
            sendMutedMessage(event.getPlayer(), punishment);
            event.setCancelled(true);
            return;
        }
        MatchTeam matchTeam = teamManagerModule.getTeam(event.getPlayer());
        String prefix = playerContext.getUserProfile().getPrefix() != null ? ChatColor.translateAlternateColorCodes('&', playerContext.getUserProfile().getPrefix().trim()) + " " : "";
        event.setFormat((TGM.get().getModule(StatsModule.class).isStatsDisabled() ? "" : playerContext.getLevelString() + " ") +
                prefix + matchTeam.getColor() + event.getPlayer().getName() + ChatColor.WHITE + ": " + event.getMessage().replaceAll("%", "%%"));
        //if (!matchTeam.isSpectator()) chatLog.add(new Chat(playerContext.getUserProfile().getId().toString(), event.getPlayer().getName(), playerContext.getPlayer().getUniqueId().toString(), event.getMessage(), matchTeam.getId(), timeModule.getTimeElapsed(), false));
    }

    private void sendMutedMessage(Player player, Punishment punishment) {
        player.sendMessage(ChatColor.RED + "You have been muted for " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', punishment.getReason()) + ( punishment.getExpires() >= 0 ? ChatColor.RED + " until " + ChatColor.GRAY + new Date(punishment.getExpires()).toString() : "") + ChatColor.RED + ".");
    }

    /*
    * Fired after the onChat so other plugins can
    * edit the chat format if needed, using the
    * AsyncPlayerChatEvent#setFormat() method.
    */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChatHighPriority(AsyncPlayerChatEvent event) {
        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(event.getPlayer());
        if (!event.isCancelled())  {
            Bukkit.getOnlinePlayers().forEach(player -> {
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
            Bukkit.getConsoleSender().sendMessage(event.getFormat().replace("%%", "%"));
        }
        event.setCancelled(true);
    }

    public void sendTeamChat(PlayerContext playerContext, String message) {
        MatchTeam matchTeam = teamManagerModule.getTeam(playerContext.getPlayer());
        for (PlayerContext member : matchTeam.getMembers()) {
            member.getPlayer().sendMessage(matchTeam.getColor() + "[" + matchTeam.getAlias() + "] "
                    + playerContext.getPlayer().getName() + ChatColor.WHITE + ": " + message);
        }
        //if (!matchTeam.isSpectator()) chatLog.add(new Chat(playerContext.getUserProfile().getId().toString(), playerContext.getPlayer().getName(), playerContext.getPlayer().getUniqueId().toString(), message, matchTeam.getId(), timeModule.getTimeElapsed(), true));
    }

    @Override
    public void unload() {
        chatLog.clear();
    }
}
