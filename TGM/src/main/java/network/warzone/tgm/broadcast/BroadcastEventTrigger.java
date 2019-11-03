package network.warzone.tgm.broadcast;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.MatchLoadEvent;
import network.warzone.tgm.match.MatchResultEvent;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.user.PlayerContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by Jorge on 4/16/2018.
 */
public class BroadcastEventTrigger implements Listener{

    private BroadcastManager manager;

    BroadcastEventTrigger(BroadcastManager manager) {
        this.manager = manager;
        Bukkit.getPluginManager().registerEvents(this, TGM.get());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(event.getPlayer());
        if (playerContext.getUserProfile().isNew()) {
            trigger(event.getPlayer(), "onFirstJoin");
            return;
        }
        trigger(event.getPlayer(), "onJoin");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMatchCycle(MatchResultEvent event) {
        trigger("onMatchResult");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeamJoin(TeamChangeEvent event) {
        if (event.isCancelled()) return;
        if (event.getOldTeam() == null || event.getTeam().isSpectator()) return;
        trigger(event.getPlayerContext().getPlayer(), "onTeamJoin");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMatchLoad(MatchLoadEvent event) {
        trigger("onMatchLoad");
    }

    private void trigger(Player player, String name) {
        this.manager.getOnEvents(name).forEach(broadcast -> this.manager.broadcast(player, broadcast));
    }

    private void trigger(String name) {
        this.manager.getOnEvents(name).forEach(broadcast -> this.manager.broadcast(broadcast));
    }

}
