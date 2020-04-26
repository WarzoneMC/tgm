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
import org.bukkit.Location;
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
    private final boolean inverted;

    @EventHandler
    public void onShootBowTest(EntityShootBowEvent e) {
        for (Region region : regions) {
            if (contains(region, e.getEntity().getLocation())) {
                FilterResult filterResult = evaluator.evaluate(e.getEntity());
                if (filterResult == FilterResult.DENY) {
                    e.setCancelled(true);
                    if (message != null) e.getEntity().sendMessage(message);
                } else if (filterResult == FilterResult.ALLOW) {
                    e.setCancelled(false);
                }
            }
        }
    }

    private boolean contains(Region region, Location location) {
        if (!inverted) return region.contains(location);
        else return !region.contains(location);
    }

    public static UseBowFilterType parse(Match match, JsonObject jsonObject) {
        List<MatchTeam> matchTeams = match.getModule(TeamManagerModule.class).getTeams(jsonObject.get("teams").getAsJsonArray());
        List<Region> regions = new ArrayList<>();

        for (JsonElement regionElement : jsonObject.getAsJsonArray("regions")) {
            Region region = match.getModule(RegionManagerModule.class).getRegion(match, regionElement);
            if (region != null) {
                regions.add(region);
            }
        }

        FilterEvaluator filterEvaluator = FilterManagerModule.initEvaluator(match, jsonObject);
        String message = jsonObject.has("message") ? ChatColor.translateAlternateColorCodes('&', jsonObject.get("message").getAsString()) : null;
        boolean inverted = jsonObject.has("inverted") && jsonObject.get("inverted").getAsBoolean();
        return new UseBowFilterType(matchTeams, regions, filterEvaluator, message, inverted);
    }
}
