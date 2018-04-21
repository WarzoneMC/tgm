package network.warzone.tgm.broadcast;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.MatchResultEvent;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

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
        trigger(event.getPlayer(), "onJoin");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMatchCycle(MatchResultEvent event) {
        Bukkit.getOnlinePlayers().forEach(player -> trigger(player, "onMatchResult"));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeamJoin(TeamChangeEvent event) {
        if (event.getOldTeam() == null || event.getTeam().isSpectator()) return;
        trigger(event.getPlayerContext().getPlayer(), "onTeamJoin");
    }

    private void trigger(Player player, String name) {
        List<Broadcast> broadcasts = this.manager.getOnEvents(name);
        for (Broadcast broadcast : broadcasts) {
            this.manager.broadcast(player, broadcast);
        }
    }

}
