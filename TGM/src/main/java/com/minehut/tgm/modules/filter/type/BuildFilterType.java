package com.minehut.tgm.modules.filter.type;

import com.minehut.tgm.modules.filter.FilterResult;
import com.minehut.tgm.modules.filter.evaluate.FilterEvaluator;
import com.minehut.tgm.modules.region.Region;
import com.minehut.tgm.modules.team.MatchTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;

@AllArgsConstructor
public class BuildFilterType implements FilterType, Listener {
    @Getter private final List<MatchTeam> teams;
    @Getter private final List<Region> regions;
    @Getter private final FilterEvaluator evaluator;
    @Getter private final String message;

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        for (Region region : regions) {
            if (region.contains(event.getBlockPlaced().getLocation())) {
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
    public void onBlockBreak(BlockBreakEvent event) {
        for (Region region : regions) {
            if (region.contains(event.getBlock().getLocation())) {
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
}
