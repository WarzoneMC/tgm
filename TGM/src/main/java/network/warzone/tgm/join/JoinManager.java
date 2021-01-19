package network.warzone.tgm.join;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.map.MapRotationFile;
import network.warzone.tgm.map.Rotation;
import network.warzone.tgm.match.MatchPostLoadEvent;
import network.warzone.tgm.modules.chat.ChatModule;
import network.warzone.tgm.nickname.Nick;
import network.warzone.tgm.nickname.NickManager;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.Ranks;
import network.warzone.warzoneapi.models.PlayerLogin;
import network.warzone.warzoneapi.models.Punishment;
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

import com.destroystokyo.paper.Title;

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
                    + ChatColor.AQUA + "Appeal at " + TGM.get().getConfig().getString("server.appeal") + "\n"
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

        String name = null;
        Skin skin = null;

        // Get the optional nick for the player.
        Optional<Nick> optionalNick = nickManager.getNick(playerContext);

        // Check if the the nick is present and active.
        if (optionalNick.isPresent() && optionalNick.get().isApplied()) {
            // Get the nick.
            Nick queuedNick = optionalNick.get();

            name = queuedNick.getName();
            skin = queuedNick.getSkin();

            nickManager.update(playerContext, nick -> nick.setActive(true));
        }

        // 1. Username you're nicked as is online.
        if (name != null && Bukkit.getPlayer(name) != null) {
            name = null;
            nickManager.getNick(playerContext).ifPresent(nick -> nick.setName(null));
            p.sendMessage(ChatColor.RED + "The username you are nicked as is online so your nickname has been removed.");
        }
        // 2. You are joining and a player nicked as you is online.
        if (nickManager.isNickName(p.getName())) {
            nickManager.getNicks()
                    .stream()
                    .filter(nick -> nick.getName().equals(p.getName()))
                    .findFirst()
                    .ifPresent(
                            nick -> {
                                Player offender = Bukkit.getPlayer(nick.getOriginalName());
                                if (offender != null) offender.sendMessage(ChatColor.RED + "The player you are nicked as has joined. Your nick must be reset.");
                                nickManager.reset(TGM.get().getPlayerManager().getPlayerContext(offender), true);
                            }
                    );
        }

        if (name != null) {
            try {
                nickManager.setName(playerContext, name);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }
        if (skin != null) {
            nickManager.setSkin(playerContext, skin);
        }

        String joinMsg;
        if (event.getPlayer().hasPermission("tgm.donator.joinmsg") && !playerContext.getUserProfile().isStaff() && playerContext.getUserProfile().getPrefix() != null) {
            String prefix = playerContext.getUserProfile().getPrefix() != null ? ChatColor.translateAlternateColorCodes('&', playerContext.getUserProfile().getPrefix().trim()) + " " : "";
            joinMsg = ChatColor.GOLD + prefix + playerContext.getDisplayName() + ChatColor.GOLD + " joined.";
            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f));
        } else joinMsg = ChatColor.GRAY + playerContext.getDisplayName() + " joined.";

        if (playerContext.getUserProfile().isNew()) {
            joinMsg += ChatColor.LIGHT_PURPLE + " [NEW]";
            event.getPlayer()
                    .sendTitle(new Title("", ChatColor.translateAlternateColorCodes('&', "&7Use &b/join&7 to play!")));
        }
        
        event.setJoinMessage(joinMsg);

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
