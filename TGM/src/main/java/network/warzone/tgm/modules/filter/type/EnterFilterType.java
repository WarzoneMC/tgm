package network.warzone.tgm.modules.filter.type;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.filter.FilterResult;
import network.warzone.tgm.modules.filter.evaluate.FilterEvaluator;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.team.MatchTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

@AllArgsConstructor @Getter
public class EnterFilterType implements FilterType, Listener {
    private final List<MatchTeam> teams;
    private final List<Region> regions;
    private final FilterEvaluator evaluator;
    private final String message;

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        for (Region region : regions) {
            if (region.contains(event.getTo())) {
                for (MatchTeam matchTeam : teams) {
                    if (matchTeam.containsPlayer(event.getPlayer())) {
                        FilterResult filterResult = evaluator.evaluate(event.getPlayer());
                        if(TGM.get().getMatchManager().getMatch().getMatchStatus() == MatchStatus.OVERTIME || TGM.get().getMatchManager().getMatch().getMatchStatus() == MatchStatus.POST) filterResult = FilterResult.ALLOW;
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
}
