package network.warzone.tgm.modules;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

@Getter
public class ChatModule extends MatchModule implements Listener {

    @AllArgsConstructor @NoArgsConstructor
    public enum Channel {
        ALL, TEAM, STAFF("tgm.staffchat");

        private String permission;

        public boolean hasPermission(Player player) {
            return permission == null || player.hasPermission(permission);
        }

        public static Channel byName(String name) {
            for (Channel channel : values()) {
                if (channel.name().equalsIgnoreCase(name)) {
                    return channel;
                }
            }
            return null;
        }
    }

    private TeamManagerModule teamManagerModule;
    private TimeModule timeModule;
    private final List<Chat> chatLog = new ArrayList<>();
    private static final Map<String, Channel> channels = new HashMap<>();

    @Override
    public void load(Match match) {
        teamManagerModule = match.getModule(TeamManagerModule.class);
        timeModule = match.getModule(TimeModule.class);
    }

    public static Map<String, Channel> getChannels() {
        return channels;
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
        // Run this code if the chat is currently muted
        if (!TGM.get().getConfig().getBoolean("chat.enabled") && !event.getPlayer().hasPermission("tgm.chat.bypass")) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', TGM.get().getConfig().getString("chat.messages.muted")));
            event.setCancelled(true);
            return;
        }

        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(event.getPlayer());
        if (playerContext.getUserProfile().getLatestMute() != null && playerContext.getUserProfile().getLatestMute().isActive()) {
            Punishment punishment = playerContext.getUserProfile().getLatestMute();
            sendMutedMessage(event.getPlayer(), punishment);
            event.setCancelled(true);
            return;
        }

        if(!(channels.containsKey(event.getPlayer().getUniqueId().toString()))) {
            channels.put(event.getPlayer().getUniqueId().toString(), Channel.ALL);
        }

        Channel channel = channels.get(event.getPlayer().getUniqueId().toString());

        if(channel == Channel.TEAM) {
            TGM.get().getModule(ChatModule.class).sendTeamChat(playerContext, event.getMessage());
            event.setCancelled(true);
            return;
        }

        if(channel == Channel.STAFF) {
            String prefix;
            prefix = playerContext.getPrefix() != null ? ChatColor.translateAlternateColorCodes('&', playerContext.getPrefix().trim()) + " " : "";

            String result = ChatColor.DARK_RED + "[STAFF] " + prefix + ChatColor.GRAY + event.getPlayer().getName() + ": " + ChatColor.RESET + event.getMessage();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("tgm.staffchat")) player.sendMessage(result);
            }
            Bukkit.getConsoleSender().sendMessage(result);
            event.setCancelled(true);
            return;
        }

        if (channel == Channel.ALL) {
            MatchTeam matchTeam = teamManagerModule.getTeam(event.getPlayer());
            String prefix = playerContext.getPrefix() != null ? ChatColor.translateAlternateColorCodes('&', playerContext.getPrefix().trim()) + " " : "";
            event.setFormat((TGM.get().getModule(StatsModule.class).isStatsDisabled() ? "" : playerContext.getLevelString() + " ") +
                    prefix + matchTeam.getColor() + event.getPlayer().getName() + ChatColor.WHITE + ": " + event.getMessage().replaceAll("%", "%%"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        channels.remove(event.getPlayer().getUniqueId().toString());
    }

    private void sendMutedMessage(Player player, Punishment punishment) {
        player.sendMessage(ChatColor.RED + "You are currently muted for " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', punishment.getReason()) + ( punishment.getExpires() >= 0 ? ChatColor.RED + " until " + ChatColor.GRAY + new Date(punishment.getExpires()).toString() : "") + ChatColor.RED + ".");
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
                        new TextComponent("\n" + ChatColor.AQUA + "XP: " + ChatColor.RESET + playerContext.getUserProfile().getXP()),
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
