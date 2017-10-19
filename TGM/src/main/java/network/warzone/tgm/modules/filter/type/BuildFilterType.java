package network.warzone.tgm.modules.filter.type;

import network.warzone.tgm.modules.filter.FilterResult;
import network.warzone.tgm.modules.filter.evaluate.FilterEvaluator;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.team.MatchTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.List;

@AllArgsConstructor @Getter
public class BuildFilterType implements FilterType, Listener {

    private final List<MatchTeam> teams;
    private final List<Region> regions;
    private final FilterEvaluator evaluator;
    private final String message;

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

    @EventHandler
    public void onPlayerClickItemFram(PlayerInteractEntityEvent event) {
        if (!event.isCancelled() && event.getRightClicked() != null && event.getRightClicked() instanceof ItemFrame) {
            for (Region region : regions) {
                if (region.contains(event.getRightClicked().getLocation())) {
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

    @EventHandler
    public void onHangingDamage(EntityDamageByEntityEvent event) {
        if (!event.isCancelled() && event.getEntity() != null && event.getEntity() instanceof ItemFrame) {
            if (event.getDamager() instanceof Player) {
                Player player = ((Player) event.getDamager()).getPlayer();

                for (Region region : regions) {
                    if (region.contains(player.getLocation())) {
                        for (MatchTeam matchTeam : teams) {
                            if (matchTeam.containsPlayer(player)) {
                                FilterResult filterResult = evaluator.evaluate(player);
                                if (filterResult == FilterResult.DENY) {
                                    event.setCancelled(true);
                                    player.sendMessage(message);
                                } else if (filterResult == FilterResult.ALLOW) {
                                    event.setCancelled(false);
                                }
                            }
                        }
                    }
                }
            } else {
                for (Region region : regions) {
                    if (region.contains(event.getEntity().getLocation())) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onHangingDamage(HangingBreakEvent event) {
        if (!event.isCancelled()) {
            for (Region region : regions) {
                if (region.contains(event.getEntity().getLocation())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (!event.isCancelled()) {
            FilterResult filterResult = evaluator.evaluate();
            if (filterResult == FilterResult.DENY) {
                for (Region region : regions) {
                    if (region.contains(event.getBlock().getLocation().clone().add(event.getDirection().getModX(), event.getDirection().getModY(), event.getDirection().getModZ()))) {
                        event.setCancelled(true);
                        return;
                    } else {
                        for (Block block : event.getBlocks()) {
                            if (region.contains(event.getBlock().getLocation().clone().add(event.getDirection().getModX(), event.getDirection().getModY(), event.getDirection().getModZ())) || region.contains(block.getLocation().clone().add(event.getDirection().getModX(), event.getDirection().getModY(), event.getDirection().getModZ()))) {
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!event.isCancelled()) {
            FilterResult filterResult = evaluator.evaluate();
            if (filterResult == FilterResult.DENY) {
                for (Region region : regions) {
                    if (region.contains(event.getBlock().getLocation().clone().add(event.getDirection().getOppositeFace().getModX(), event.getDirection().getOppositeFace().getModY(), event.getDirection().getOppositeFace().getModZ()))) {
                        event.setCancelled(true);
                        return;
                    } else {
                        for (Block block : event.getBlocks()) {
                            if (region.contains(block.getLocation().clone().add(event.getDirection().getOppositeFace().getModX(), event.getDirection().getOppositeFace().getModY(), event.getDirection().getOppositeFace().getModZ()))) {
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

}
