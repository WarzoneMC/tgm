package network.warzone.tgm.modules;

import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TeamJoinNotificationsModule extends MatchModule implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeamJoin(TeamChangeEvent event) {
        if (!event.isCancelled())
            event.getPlayerContext().getPlayer().sendMessage(ChatColor.WHITE + "You joined " + event.getTeam().getColor() + ChatColor.BOLD + event.getTeam().getAlias());
    }
}
