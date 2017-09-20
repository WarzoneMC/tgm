package com.minehut.tgm.modules.filter.type;

import com.minehut.tgm.modules.filter.FilterResult;
import com.minehut.tgm.modules.filter.evaluate.FilterEvaluator;
import com.minehut.tgm.modules.region.Region;
import com.minehut.tgm.modules.team.MatchTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorge on 9/9/2017.
 */
@AllArgsConstructor
public class BlockExplodeFilterType implements FilterType, Listener {
    @Getter private final List<Region> regions;
    @Getter private final FilterEvaluator evaluator;

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
