package network.warzone.tgm.join;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import network.warzone.tgm.TGM;
import network.warzone.tgm.map.MapRotationFile;
import network.warzone.tgm.map.Rotation;
import network.warzone.tgm.match.MatchPostLoadEvent;
import network.warzone.tgm.modules.chat.ChatConstant;
import network.warzone.tgm.modules.chat.ChatModule;
import network.warzone.tgm.nickname.NickManager;
import network.warzone.tgm.nickname.QueuedNick;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.HashMaps;
import network.warzone.tgm.util.Ranks;
import network.warzone.warzoneapi.models.PlayerAltsResponse;
import network.warzone.warzoneapi.models.PlayerLogin;
import network.warzone.warzoneapi.models.Punishment;
import network.warzone.warzoneapi.models.PunishmentsListRequest;
import network.warzone.warzoneapi.models.PunishmentsListResponse;
import network.warzone.warzoneapi.models.Skin;
import network.warzone.warzoneapi.models.UserProfile;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by luke on 4/27/17.
 */
@Getter
public class JoinManager implements Listener {

    private Collection<QueuedJoin> queuedJoins = new ConcurrentLinkedQueue<>();
    private Set<LoginService> loginServices = new HashSet<>();

    public JoinManager() {
        TGM.registerEvents(this);

        //empty queued joins when the connection didn't follow through for an unknown reason.
        Bukkit.getScheduler().runTaskTimerAsynchronously(TGM.get(), () -> {
            queuedJoins.removeIf(queuedJoin -> System.currentTimeMillis() - queuedJoin.getTime() > 10 * 1000);
        }, 20 * 10L, 20 * 10L);
    }

    /**
     * Allow custom services to hook into the login event.
     * This can be used with an external tournament plugin.
     */
    public void addLoginService(LoginService loginService) {
        getLoginServices().add(loginService);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        UserProfile userProfile = TGM.get().getTeamClient().login(new PlayerLogin(event.getName(), uuid.toString(), event.getAddress().getHostAddress()));

        Bukkit.getLogger().info(userProfile.getName() + " " + userProfile.getId().toString() + " | ranks: " + userProfile.getRanksLoaded().size() + "/" + userProfile.getRanks().size() + " (loaded/total)");

        //TODO Custom ban messages
        Punishment punishment = userProfile.getLatestBan();
        if (punishment != null) {
            event.setKickMessage(ChatColor.RED + "You have been banned from the server. Reason:\n"
                    + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', punishment.getReason()) + "\n\n"
                    + ChatColor.RED + "Ban expires: " + ChatColor.RESET + (punishment.getExpires() >= 0 ? new Date(punishment.getExpires()).toString() : "Never") + "\n"
                    + ChatColor.AQUA + "Appeal at https://discord.io/WarzoneMC\n"
                    + ChatColor.GRAY + "ID: " + punishment.getId().toString()
            );
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            return;
        }
        //Bukkit.getLogger().info(userProfile.getName() + " " + userProfile.getId().toString()); //Already logged above

        queuedJoins.add(new QueuedJoin(uuid, userProfile, System.currentTimeMillis()));
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        QueuedJoin queuedJoin = getQueuedUserProfile(uuid);
        if (queuedJoin == null) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(ChatColor.RED + "Unable to load user profile. Please try again.");
            return;
        }

        PlayerContext playerContext = new PlayerContext(event.getPlayer(), queuedJoin.getUserProfile());
        TGM.get().getPlayerManager().addPlayer(playerContext);

        Ranks.createAttachment(event.getPlayer());
        playerContext.getUserProfile(true).getRanksLoaded().forEach(rank -> Ranks.addPermissions(event.getPlayer(), rank.getPermissions()));

        loginServices.forEach(loginService -> loginService.login(playerContext));
        queuedJoins.remove(queuedJoin);
    }

    private QueuedJoin getQueuedUserProfile(UUID uuid) {
        return queuedJoins.stream().filter(queuedJoin -> uuid.equals(queuedJoin.getUuid())).findFirst().orElse(null);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(event.getPlayer());
        Bukkit.getPluginManager().callEvent(new MatchJoinEvent(playerContext));

        Player p = playerContext.getPlayer();
        NickManager nickManager = TGM.get().getNickManager();
        // Check if a player is nicked as your name.
        if (nickManager.getNickNames().containsValue(p.getName())) {
            // Get UUID for player currently with the nickname.
            UUID uuid = HashMaps.reverseGetFirst(p.getName(), nickManager.getNickNames());

            // Get the player by the UUID.
            Player player = Bukkit.getPlayer(uuid);

            // Check if the user is online
            if (player != null && player.isOnline()) {
                player.sendMessage(ChatColor.RED + "Your name must be reset because the player has joined!");
                try {
                    nickManager.reset(player, false);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    p.sendMessage(ChatConstant.ERROR_RATE_LIMITED.toString());
                }
            }
        }

        String name = nickManager.getNickNames().get(p.getUniqueId());
        Skin skin = nickManager.getSkins().get(p.getUniqueId());

        // Get the optional queued nick for the player.
        Optional<QueuedNick> optionalQueuedNick = nickManager.getQueuedNick(p);

        // Check if the the queued nick is present.
        if (optionalQueuedNick.isPresent()) {
            // Get the queued nick.
            QueuedNick queuedNick = optionalQueuedNick.get();

            name = queuedNick.getName();
            skin = queuedNick.getSkin();

            // Remove the queued nick.
            nickManager.getQueuedNicks().remove(queuedNick);
        }

        // Check if a player is nicked as the queued nick.
        if (nickManager.getNickNames().containsValue(name)) {
            // Get UUID for player currently with the nickname.
            UUID uuid = HashMaps.reverseGetFirst(name, nickManager.getNickNames());

            // Get the player by the UUID.
            Player player = Bukkit.getPlayer(uuid);

            // Check if the player is online.
            if (player != null && !uuid.equals(p.getUniqueId()) && player.isOnline()) {
                p.sendMessage(ChatColor.RED + "Could not apply the queued nick. The player is already online!");
                try {
                    nickManager.reset(p, false);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    p.sendMessage(ChatConstant.ERROR_RATE_LIMITED.toString());
                }
                // Invalidate the nick.
                name = null;
                skin = null;
            }
        }

        if (name != null) {
            try {
                nickManager.setName(p, name);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }
        if (skin != null) {
            nickManager.setSkin(p, skin);
        }

        String joinMsg;
        if (event.getPlayer().hasPermission("tgm.donator.joinmsg") && !playerContext.getUserProfile().isStaff() && playerContext.getUserProfile().getPrefix() != null) {
            String prefix = playerContext.getUserProfile().getPrefix() != null ? ChatColor.translateAlternateColorCodes('&', playerContext.getUserProfile().getPrefix().trim()) + " " : "";
            joinMsg = ChatColor.GOLD + prefix + playerContext.getDisplayName() + ChatColor.GOLD + " joined.";
            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f));
        } else joinMsg = ChatColor.GRAY + playerContext.getDisplayName() + " joined.";

        if (playerContext.getUserProfile().isNew()) joinMsg += ChatColor.LIGHT_PURPLE + " [NEW]";
        event.setJoinMessage(joinMsg);

        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> { // Check alts asynchronously to prevent delaying the join event
            PlayerAltsResponse response = TGM.get().getTeamClient().getAlts(event.getPlayer().getName());
            if (response != null && !response.isError() && !response.getUsers().isEmpty()) {
                List<String> punishedAlts = new ArrayList<>();

                boolean banned = false;

                for (UserProfile user : response.getUsers()) {
                    PunishmentsListResponse punishmentsListResponse = TGM.get().getTeamClient().getPunishments(new PunishmentsListRequest(user.getName(), null));
                    if (!punishmentsListResponse.isNotFound()) {
                        boolean altBanned = false;
                        boolean altMuted = false;

                        for (Punishment punishment : punishmentsListResponse.getPunishments()) {
                            if ("BAN".equals(punishment.getType().toUpperCase()) && punishment.isActive()) {
                                altBanned = true;
                            }

                            if ("MUTE".equals(punishment.getType().toUpperCase()) && punishment.isActive()) {
                                altMuted = true;
                            }
                        }

                        if (altBanned) {
                            banned = true;
                            punishedAlts.add(ChatColor.GRAY + "- " + ChatColor.RED + user.getName());
                        } else if (altMuted) {
                            punishedAlts.add(ChatColor.GRAY + "- " + ChatColor.YELLOW + user.getName());
                        }
                    }
                }

                if (punishedAlts.isEmpty()) return;

                String staffNotification = ChatColor.DARK_RED + "[STAFF] " + (banned ? ChatColor.RED : ChatColor.YELLOW) +
                        event.getPlayer().getName() + " might be " + (banned ? "ban" : "mute") + "-evading";

                TextComponent message = new TextComponent(staffNotification);

                TextComponent[] hoverMessage = new TextComponent[punishedAlts.size()];
                for (int i = 0; i < punishedAlts.size(); i++) {
                    hoverMessage[i] = new TextComponent(punishedAlts.get(i));
                }

                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessage));

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("tgm.lookup")) {
                        player.spigot().sendMessage(message);
                    }
                }
                Bukkit.getConsoleSender().sendMessage(message);
            }
        });
        handleRotationUpdate(false);
    }

    //TODO: Persistent modules
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setFormat("");
        if (event.isCancelled()) return;
        event.setCancelled(TGM.get().getModule(ChatModule.class) == null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCycle(MatchPostLoadEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(player);
            Bukkit.getPluginManager().callEvent(new MatchJoinEvent(playerContext));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(ChatColor.GRAY + event.getPlayer().getName() + " left.");
        handleQuit(event.getPlayer());
        handleRotationUpdate(true);
    }

    private void handleRotationUpdate(boolean isLeaving) {
        int playerCount = Bukkit.getOnlinePlayers().size() - (isLeaving ? 1 : 0);
        MapRotationFile rotationFile = TGM.get().getMatchManager().getMapRotation();

        if (!rotationFile.getRotation().isDefault()) return;
        Rotation potentialRotation = rotationFile.getRotationForPlayerCount(playerCount);

        if (potentialRotation != rotationFile.getRotation()) {
            System.out.println("Rotation has changed to " + potentialRotation.getName() + " from " + rotationFile.getRotation().getName());
            Bukkit.getOnlinePlayers().forEach(
                    player -> player.sendMessage(
                            ChatColor.GRAY + "The rotation has been updated to " + ChatColor.GOLD + potentialRotation.getName() + ChatColor.GRAY + " to accommodate for the new player size."
                    )
            );

            rotationFile.setRotation(potentialRotation.getName());
        }
    }

    private void handleQuit(Player player) {
        TGM.get().getPlayerManager().removePlayer(TGM.get().getPlayerManager().getPlayerContext(player));
        Ranks.removeAttachment(player);
    }

    @AllArgsConstructor @Getter
    private class QueuedJoin {
        private UUID uuid;
        private UserProfile userProfile;
        private long time;
    }

}
