package network.warzone.tgm.modules.portal;

import lombok.AllArgsConstructor;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.team.MatchTeam;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@AllArgsConstructor
public class PortalModule extends MatchModule implements Listener {
    private Region from;
    private Location to;
    private MatchTeam team;
    private boolean sound;

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (team != null) {
            if (!team.containsPlayer(event.getPlayer())) {
                return;
            }
        }

        if (from.contains(event.getTo()) && !from.contains(event.getFrom())) {
            event.getPlayer().teleport(to);
            if (sound) {
                event.getFrom().getWorld().playSound(event.getFrom(), Sound.ENTITY_ENDERMEN_TELEPORT, 0.2f, 1);
                event.getTo().getWorld().playSound(event.getFrom(), Sound.ENTITY_ENDERMEN_TELEPORT, 0.2f, 1);
            }
        }
    }
}
