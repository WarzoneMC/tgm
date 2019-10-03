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
import network.warzone.tgm.util.Strings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorge on 10/02/2019
 */
@AllArgsConstructor @Getter
public class BlockPlaceFilterType implements FilterType, Listener {

    private final List<MatchTeam> teams;
    private final List<Region> regions;
    private final FilterEvaluator evaluator;
    private final String message;
    private final List<Material> blocks;
    private final boolean inverted;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        for (Region region : regions) {
            if (contains(region, event.getBlockPlaced().getLocation())) {
                for (MatchTeam matchTeam : teams) {
                    if (matchTeam.containsPlayer(event.getPlayer())) {
                        FilterResult filterResult = evaluator.evaluate(event.getPlayer());
                        if (!canPlace(event, filterResult)) {
                            event.setCancelled(true);
                            event.getPlayer().sendMessage(message);
                        }
                        break;
                    }
                }
            }
        }
    }

    private boolean contains(Region region, Location location) {
        if (!inverted) return region.contains(location);
        else return !region.contains(location);
    }


    private boolean canPlace(BlockPlaceEvent event, FilterResult filterResult) {
        if (filterResult == FilterResult.ALLOW) {
            if (blocks == null || blocks.isEmpty()) return true;
            return blocks.contains(event.getBlockPlaced().getType());
        } else {
            if (blocks == null || blocks.isEmpty()) return false;
            return !blocks.contains(event.getBlockPlaced().getType());
        }
    }

    public static BlockPlaceFilterType parse(Match match, JsonObject jsonObject) {
        List<MatchTeam> matchTeams = Parser.getTeamsFromElement(match.getModule(TeamManagerModule.class), jsonObject.get("teams"));
        List<Region> regions = new ArrayList<>();
        List<Material> blocks = new ArrayList<>();

        for (JsonElement regionElement : jsonObject.getAsJsonArray("regions")) {
            Region region = match.getModule(RegionManagerModule.class).getRegion(match, regionElement);
            if (region != null) {
                regions.add(region);
            }
        }
        if (jsonObject.has("blocks"))
            for (JsonElement materialElement : jsonObject.getAsJsonArray("blocks")) {
                if (!materialElement.isJsonPrimitive()) continue;
                Material material = Material.getMaterial(Strings.getTechnicalName(materialElement.getAsString()));
                if (material == null) continue;
                blocks.add(material);
            }

        FilterEvaluator filterEvaluator = FilterManagerModule.initEvaluator(match, jsonObject);
        String message = ChatColor.translateAlternateColorCodes('&', jsonObject.get("message").getAsString());
        boolean inverted = jsonObject.has("inverted") && jsonObject.get("inverted").getAsBoolean();
        return new BlockPlaceFilterType(matchTeams, regions, filterEvaluator, message, blocks, inverted);
    }

}
