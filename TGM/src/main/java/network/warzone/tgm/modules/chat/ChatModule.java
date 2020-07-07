package network.warzone.tgm.modules.chat;

import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.StatsModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.time.TimeModule;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.warzoneapi.models.Chat;
import network.warzone.warzoneapi.models.Punishment;
import network.warzone.warzoneapi.models.UserProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

    private static final List<String> blockedCmds = Arrays.asList("t ", "w ", "r ", "reply", "minecraft:w", "tell", "minecraft:tell", "minecraft:t ", "msg", "minecraft:msg");

    private TeamManagerModule teamManagerModule;
    private TimeModule timeModule;
    private final List<Chat> chatLog = new ArrayList<>();
    private static final Map<String, ChatChannel> channels = new HashMap<>();

    @Override
    public void load(Match match) {
        teamManagerModule = match.getModule(TeamManagerModule.class);
        timeModule = match.getModule(TimeModule.class);
    }

    public static Map<String, ChatChannel> getChannels() {
        return channels;
    }

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
            channels.put(event.getPlayer().getUniqueId().toString(), ChatChannel.ALL);
        }

        ChatChannel channel = channels.get(event.getPlayer().getUniqueId().toString());

        if(channel == ChatChannel.TEAM) {
            TGM.get().getModule(ChatModule.class).sendTeamChat(playerContext, event.getMessage());
            event.setCancelled(true);
            return;
        }

        if(channel == ChatChannel.STAFF) {
            String prefix;
            prefix = playerContext.getUserProfile().getPrefix() != null ? ChatColor.translateAlternateColorCodes('&', playerContext.getUserProfile().getPrefix().trim()) + " " : "";

            sendStaffMessage(prefix, event.getPlayer().getName(), event.getMessage());
            event.setCancelled(true);
            return;
        }

        if (channel == ChatChannel.ALL) {
            MatchTeam matchTeam = teamManagerModule.getTeam(event.getPlayer());
            UserProfile userProfile = playerContext.getUserProfile();
            String prefix = userProfile.getPrefix() != null ? ChatColor.translateAlternateColorCodes('&', userProfile.getPrefix().trim()) + " " : "";
            StringBuilder format = new StringBuilder();
            if (!TGM.get().getModule(StatsModule.class).isStatsDisabled()) format.append(playerContext.getLevelString()).append(" ");
            format.append(prefix)
                    .append(matchTeam.getColor())
                    .append(event.getPlayer().getName());
            if (!playerContext.isNicked() && userProfile.getActiveTag() != null && !"".equals(userProfile.getActiveTag()))
                format.append(ChatColor.GRAY)
                        .append(" [")
                        .append(ChatColor.translateAlternateColorCodes('&', userProfile.getActiveTag()))
                        .append(ChatColor.GRAY)
                        .append("]");
            format.append(ChatColor.WHITE)
                    .append(": ")
                    .append(event.getMessage().replaceAll("%", "%%"));
            event.setFormat(format.toString());
        }
    }

    public static void sendStaffMessage(String prefix, String sender, String message) {
        String result = ChatColor.DARK_RED + "[STAFF] " + prefix + ChatColor.GRAY + sender + ": " + ChatColor.GREEN + message;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("tgm.staffchat")) player.sendMessage(result);
        }
        Bukkit.getConsoleSender().sendMessage(result);
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
        if (!TGM.get().getConfig().getBoolean("chat.stats-hover")) return;
        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(event.getPlayer());
        if (!event.isCancelled())  {
            Bukkit.getOnlinePlayers().forEach(player -> {
                BaseComponent[] stats = new BaseComponent[]{new TextComponent(ChatColor.AQUA + "Level: " + playerContext.getLevelString()
                        .replace("[", "").replace("]", "")),
                        new TextComponent("\n"),
                        new TextComponent("\n" + ChatColor.AQUA + "XP: " + ChatColor.RESET + playerContext.getUserProfile().getXP()),
                        new TextComponent("\n" + ChatColor.AQUA + "Kills: " + ChatColor.RESET + playerContext.getUserProfile().getKills()),
                        new TextComponent("\n" + ChatColor.AQUA + "Deaths: " + ChatColor.RESET + playerContext.getUserProfile().getDeaths()),
                        new TextComponent("\n" + ChatColor.AQUA + "K/D: " + ChatColor.RESET + playerContext.getUserProfile().getKDR()),
                        new TextComponent("\n"),
                        new TextComponent("\n" + ChatColor.AQUA + "Wins: " + ChatColor.RESET + playerContext.getUserProfile().getWins()),
                        new TextComponent("\n" + ChatColor.AQUA + "Losses: " + ChatColor.RESET + playerContext.getUserProfile().getLosses()),
                        new TextComponent("\n" + ChatColor.AQUA + "W/L: " + ChatColor.RESET + playerContext.getUserProfile().getWLR())};
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, stats);

                BaseComponent[] mainComponents = TextComponent.fromLegacyText(String.format(event.getFormat(), playerContext.getPlayer().getName(), event.getMessage()));
                for (BaseComponent component : mainComponents) {
                    component.setHoverEvent(hoverEvent);
                }
                player.spigot().sendMessage(mainComponents);
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
