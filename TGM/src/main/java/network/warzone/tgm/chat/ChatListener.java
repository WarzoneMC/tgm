package network.warzone.tgm.chat;

import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.event.MatchPostLoadEvent;
import network.warzone.tgm.match.event.MatchUnloadEvent;
import network.warzone.tgm.modules.StatsModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.warzoneapi.models.Punishment;
import network.warzone.warzoneapi.models.UserProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class ChatListener implements Listener {

    private static final List<String> blockedCmds = Arrays.asList("t ", "w ", "r ", "reply", "minecraft:w", "tell", "minecraft:tell", "minecraft:t ", "msg", "minecraft:msg");

    private TeamManagerModule teamManagerModule;
    private StatsModule statsModule;
    private static final Map<String, ChatChannel> channels = new HashMap<>();
    @Getter private static final List<UUID> disabledStaffChats = new ArrayList<>();

    public ChatListener() {
        TGM.registerEvents(this);
    }

    @EventHandler
    public void onLoad(MatchPostLoadEvent event) {
        teamManagerModule = event.getMatch().getModule(TeamManagerModule.class);
        statsModule = event.getMatch().getModule(StatsModule.class);
    }

    @EventHandler
    public void onCycle(MatchUnloadEvent event) {
        teamManagerModule = null;
        statsModule = null;
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
        FileConfiguration config = TGM.get().getConfig();
        if (!config.getBoolean("chat.enabled") && !event.getPlayer().hasPermission("tgm.chat.bypass")) {
            String message = Objects.requireNonNull(config.getString("chat.messages.muted"));
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
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

        if(teamManagerModule != null && channel == ChatChannel.TEAM) {
            this.sendTeamChat(playerContext, event.getMessage());
            event.setCancelled(true);
            return;
        }

        if(channel == ChatChannel.STAFF) {
            String prefix;
            prefix = playerContext.getPrefix() != null ? ChatColor.translateAlternateColorCodes('&', playerContext.getPrefix().trim()) + " " : "";

            sendStaffMessage(prefix, event.getPlayer().getName(), event.getMessage());
            event.setCancelled(true);
            return;
        }

        if (channel == ChatChannel.ALL) {
            ChatColor teamColor;
            if (teamManagerModule == null) teamColor = ChatColor.AQUA;
            else teamColor = teamManagerModule.getTeam(event.getPlayer()).getColor();
            String prefix = playerContext.getPrefix() != null ? ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', playerContext.getPrefix().trim()) + " " : "";
            StringBuilder format = new StringBuilder();
            if (statsModule == null || !statsModule.isStatsDisabled())
                format.append(playerContext.getLevelString()).append(" ");
            UserProfile userProfile = playerContext.getUserProfile();
            format.append(prefix)
                    .append(teamColor)
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
        String result = ChatColor.DARK_RED + "[STAFF] " + ChatColor.RESET + prefix + ChatColor.GRAY + sender + ": " + ChatColor.GREEN + message;
        for (Player player : Bukkit.getOnlinePlayers().stream().filter(
                (player) -> player.hasPermission("tgm.staffchat") && !disabledStaffChats.contains(player.getUniqueId())).collect(Collectors.toSet())) {
            player.sendMessage(result);
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
    }

}
