package network.warzone.tgm.modules.portal;

import lombok.AllArgsConstructor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

@AllArgsConstructor
public class PortalModule extends MatchModule implements Listener {
    private Region from;
    private Location to;
    private List<MatchTeam> teams;
    private boolean sound;

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (from == null) return;
        if (from.getBlocks().contains(event.getTo().getBlock()) && !from.getBlocks().contains(event.getFrom().getBlock())) {
            if (!teams.isEmpty()) {

                //allow spectators to use portals
                MatchTeam spectators = TGM.get().getModule(TeamManagerModule.class).getSpectators();
                if (!spectators.containsPlayer(event.getPlayer())) {
                    boolean onTeam = false;

                    for (MatchTeam team : teams) {
                        if (team.containsPlayer(event.getPlayer())) {
                            onTeam = true;
                            break;
                        }
                    }
                    if(!onTeam) return;
                }
            }

            event.getPlayer().teleport(to);
            if (sound) {
                event.getFrom().getWorld().playSound(event.getFrom(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.2f, 1);
                event.getTo().getWorld().playSound(event.getFrom(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.2f, 1);
            }
        }
    }
}
