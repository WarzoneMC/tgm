package network.warzone.tgm.modules.filter.type;

import network.warzone.tgm.modules.filter.FilterResult;
import network.warzone.tgm.modules.filter.evaluate.FilterEvaluator;
import network.warzone.tgm.modules.region.Region;
import lombok.AllArgsConstructor;
import lombok.Getter;
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

}
