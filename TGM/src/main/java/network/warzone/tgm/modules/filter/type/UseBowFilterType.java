package network.warzone.tgm.modules.filter.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.modules.filter.FilterManagerModule;
import network.warzone.tgm.modules.filter.FilterResult;
import network.warzone.tgm.modules.filter.evaluate.FilterEvaluator;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.region.RegionManagerModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.util.Parser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

import java.util.ArrayList;
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

    public static UseBowFilterType parse(Match match, JsonObject jsonObject) {
        List<MatchTeam> matchTeams = Parser.getTeamsFromElement(match.getModule(TeamManagerModule.class), jsonObject.get("teams"));
        List<Region> regions = new ArrayList<>();

        for (JsonElement regionElement : jsonObject.getAsJsonArray("regions")) {
            Region region = match.getModule(RegionManagerModule.class).getRegion(match, regionElement);
            if (region != null) {
                regions.add(region);
            }
        }

        FilterEvaluator filterEvaluator = FilterManagerModule.initEvaluator(match, jsonObject);
        String message = ChatColor.translateAlternateColorCodes('&', jsonObject.get("message").getAsString());
        return new UseBowFilterType(matchTeams, regions, filterEvaluator, message);
    }
}
