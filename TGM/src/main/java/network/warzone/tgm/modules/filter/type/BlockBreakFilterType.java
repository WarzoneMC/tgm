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
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorge on 10/03/2019
 */
@AllArgsConstructor @Getter
public class BlockBreakFilterType implements FilterType, Listener {

    private final List<MatchTeam> teams;
    private final List<Region> regions;
    private final FilterEvaluator evaluator;
    private final String message;
    private final List<Material> blocks;

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlaceEvent(BlockBreakEvent event) {
        for (Region region : regions) {
            if (region.contains(event.getBlock().getLocation())) {
                for (MatchTeam matchTeam : teams) {
                    if (matchTeam.containsPlayer(event.getPlayer())) {
                        FilterResult filterResult = evaluator.evaluate(event.getPlayer());
                        if (!canBreak(event, filterResult)) {
                            event.setCancelled(true);
                            event.getPlayer().sendMessage(message);
                        }
                        break;
                    }
                }
            }
        }
    }

    private boolean canBreak(BlockBreakEvent event, FilterResult filterResult) {
        if (filterResult == FilterResult.ALLOW) {
            if (blocks == null || blocks.isEmpty()) return true;
            return blocks.contains(event.getBlock().getType());
        } else {
            if (blocks == null || blocks.isEmpty()) return false;
            return !blocks.contains(event.getBlock().getType());
        }
    }

    public static BlockBreakFilterType parse(Match match, JsonObject jsonObject) {
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
        return new BlockBreakFilterType(matchTeams, regions, filterEvaluator, message, blocks);
    }
}
