package network.warzone.tgm.modules.filter.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.modules.filter.FilterResult;
import network.warzone.tgm.modules.filter.evaluate.FilterEvaluator;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.team.MatchTeam;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.List;

@AllArgsConstructor @Getter
public class LeaveFilterType implements FilterType, Listener {

    private final List<MatchTeam> teams;
    private final List<Region> regions;
    private final FilterEvaluator evaluator;
    private final String message;

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getX() == event.getTo().getX() && event.getFrom().getY() == event.getTo().getY() && event.getFrom().getZ() == event.getTo().getZ()) return;
        for (Region region : regions) {
            if (!region.contains(event.getTo())) {
                for (MatchTeam matchTeam : teams) {
                    if (matchTeam.containsPlayer(event.getPlayer())) {
                        FilterResult filterResult = evaluator.evaluate(event.getPlayer());
                        if (filterResult == FilterResult.DENY) {
                            event.setCancelled(true);
                            event.getPlayer().sendMessage(message);
                        } else if (filterResult == FilterResult.ALLOW) {
                            event.setCancelled(false);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBoatMove(VehicleMoveEvent event) {
        if (event.getFrom().getX() == event.getTo().getX() && event.getFrom().getY() == event.getTo().getY() && event.getFrom().getZ() == event.getTo().getZ()) return;
        for (Entity passenger : event.getVehicle().getPassengers()) {
            if (passenger instanceof Player) {
                Player player = (Player) passenger;
                for (Region region : regions) {
                    if (!region.contains(event.getTo())) {
                        for (MatchTeam matchTeam : teams) {
                            if (matchTeam.containsPlayer(player)) {
                                FilterResult filterResult = evaluator.evaluate(player);
                                if (filterResult == FilterResult.DENY) {
                                    event.getVehicle().teleport(event.getFrom().add(event.getTo().subtract(event.getFrom()).toVector().normalize().multiply(-1)));
                                    player.sendMessage(message);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
