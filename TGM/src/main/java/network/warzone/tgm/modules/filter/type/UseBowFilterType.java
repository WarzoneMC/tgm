package network.warzone.tgm.modules.filter.type;

import network.warzone.tgm.modules.filter.FilterResult;
import network.warzone.tgm.modules.filter.evaluate.FilterEvaluator;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.team.MatchTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

import java.util.List;

/**
 * Created by Vice & Thrasilias on 9/12/2018.
 */

@AllArgsConstructor @Getter
public class UseBowFilterType implements FilterType, Listener {

    private final List<MatchTeam> teams;
    private final List<Region> regions;
    private final FilterEvaluator evaluator;
    private final String message;

    @EventHandler
    public void onShootBowTest(EntityShootBowEvent e) {
        for (Region region : regions) {
            if (region.contains(e.getEntity().getLocation())) {
                FilterResult filterResult = evaluator.evaluate(e.getEntity());
                if (filterResult == FilterResult.DENY) {
                    e.setCancelled(true);
                    e.getEntity().sendMessage(message);
                } else if (filterResult == FilterResult.ALLOW) {
                    e.setCancelled(false);
                }
            }
        }
    }
}
