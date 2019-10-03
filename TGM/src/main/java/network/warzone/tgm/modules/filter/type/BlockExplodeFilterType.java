package network.warzone.tgm.modules.filter.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.modules.filter.FilterManagerModule;
import network.warzone.tgm.modules.filter.FilterResult;
import network.warzone.tgm.modules.filter.evaluate.FilterEvaluator;
import network.warzone.tgm.modules.region.Region;
import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.modules.region.RegionManagerModule;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorge on 9/9/2017.
 */
@AllArgsConstructor @Getter
public class BlockExplodeFilterType implements FilterType, Listener {
    private final List<Region> regions;
    private final FilterEvaluator evaluator;

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> cancelledBlocks = new ArrayList<>();
        for (Block block : event.blockList()) {
            for (Region region : regions) {
                FilterResult filterResult = evaluator.evaluate();
                if (filterResult == FilterResult.DENY) {
                    if (region.contains(block.getLocation())){
                        if (!cancelledBlocks.contains(block)) cancelledBlocks.add(block);
                    }
                }
            }
        }

        for (Block block : cancelledBlocks){
            event.blockList().remove(block);
        }
    }

    public static BlockExplodeFilterType parse(Match match, JsonObject jsonObject) {
        List<Region> regions = new ArrayList<>();
        for (JsonElement regionElement : jsonObject.getAsJsonArray("regions")) {
            Region region = match.getModule(RegionManagerModule.class).getRegion(match, regionElement);
            if (region != null) {
                regions.add(region);
            }
        }

        FilterEvaluator filterEvaluator = FilterManagerModule.initEvaluator(match, jsonObject);
        return new BlockExplodeFilterType(regions, filterEvaluator);
    }

}
