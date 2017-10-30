package network.warzone.tgm.join;

import network.warzone.warzoneapi.models.UserProfile;
import network.warzone.warzoneapi.models.PlayerLogin;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.MatchPostLoadEvent;
import network.warzone.tgm.user.PlayerContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;

/**
 * Created by luke on 4/27/17.
 */
@Getter
public class JoinManager implements Listener {

    private List<QueuedJoin> queuedJoins = new ArrayList<>();
    private List<LoginService> loginServices = new ArrayList<>();

    private Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public JoinManager() {
        TGM.registerEvents(this);

        //empty queued joins when the connection didn't follow through for an unknown reason.
        Bukkit.getScheduler().scheduleSyncRepeatingTask(TGM.get(), () -> {
            List<QueuedJoin> toRemove = new ArrayList<>();
            for (QueuedJoin queuedJoin : queuedJoins) {
                if (System.currentTimeMillis() - queuedJoin.getTime() > 10 * 1000) {
                    toRemove.add(queuedJoin);
                }
            }
            queuedJoins.removeAll(toRemove);
        }, 20 * 10L, 20 * 10L);
    }

    /**
     * Allow custom services to hook into the login event.
     * This can be used with an external tournament plugin.
     */
    public void addLoginService(LoginService loginService) {
        getLoginServices().add(loginService);
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.KICK_BANNED)) return;
        UserProfile userProfile = TGM.get().getTeamClient().login(new PlayerLogin(event.getName(), event.getUniqueId().toString(), event.getAddress().getHostAddress()));
        Bukkit.getLogger().info(userProfile.getName() + " " + userProfile.getId().toString() + " | ranks: " + userProfile.getRanksLoaded().size() + "/" + userProfile.getRanks().size() + " (loaded/total)");
        queuedJoins.add(new QueuedJoin(event.getUniqueId(), userProfile, System.currentTimeMillis()));
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        QueuedJoin queuedJoin = getQueuedUserProfile(event.getPlayer());
        if (queuedJoin == null) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(ChatColor.RED + "Unable to load user profile. Please try again.");
            return;
        }

        PlayerContext playerContext = new PlayerContext(event.getPlayer(), queuedJoin.getUserProfile());
        TGM.get().getPlayerManager().addPlayer(playerContext);

        createAttachment(event.getPlayer());
        playerContext.getUserProfile().getRanksLoaded().stream().forEach(rank -> addPermissions(event.getPlayer(), rank.getPermissions()));

        for (LoginService loginService : loginServices) {
            loginService.login(playerContext);
        }

        queuedJoins.remove(queuedJoin);
    }

    private QueuedJoin getQueuedUserProfile(Player player) {
        for (QueuedJoin queuedJoin : queuedJoins) {
            if (player.getUniqueId().equals(queuedJoin.getUuid())) {
                return queuedJoin;
            }
        }
        return null;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(event.getPlayer());
        Bukkit.getPluginManager().callEvent(new MatchJoinEvent(playerContext));
        event.setJoinMessage(ChatColor.GRAY + event.getPlayer().getName() + " joined.");
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
    }

    public void handleQuit(Player player) {
        TGM.get().getPlayerManager().removePlayer(TGM.get().getPlayerManager().getPlayerContext(player));
        removeAttachment(player);
    }

    @AllArgsConstructor @Getter
    private class QueuedJoin {
        private UUID uuid;
        private UserProfile userProfile;
        private long time;
    }

    private void createAttachment(Player player) {
        attachments.put(player.getUniqueId(), player.addAttachment(TGM.get()));
    }

    private void addPermissions(Player player, List<String> permissions) {
        permissions.stream().forEach(permission -> addPermission(player, permission));
    }

    private void addPermission(Player player, String permission) {
        if (attachments.containsKey(player.getUniqueId())) {
            attachments.get(player.getUniqueId()).setPermission(permission, true);
        }
    }

    private void removeAttachment(Player player) {
        if (attachments.containsKey(player.getUniqueId())) {
            attachments.remove(player.getUniqueId());
        }
    }

}
