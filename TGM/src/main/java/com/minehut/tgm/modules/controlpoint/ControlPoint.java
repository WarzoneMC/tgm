package com.minehut.tgm.modules.controlpoint;

import com.google.common.collect.Sets;
import com.minehut.tgm.TGM;
import com.minehut.tgm.modules.SpectatorModule;
import com.minehut.tgm.modules.region.Region;
import com.minehut.tgm.modules.region.RegionSave;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamChangeEvent;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import com.minehut.tgm.util.Blocks;
import com.minehut.tgm.util.ColorConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.BlockVector;

import java.util.*;

/**
 * Not a module! Other modules should initialize these and keep track of them.
 *
 * Must register listener on load.
 */

public class ControlPoint implements Listener {
    public static final ChatColor COLOR_NEUTRAL_TEAM = ChatColor.WHITE;

    public static final String SYMBOL_CP_INCOMPLETE = "\u29be";     // ⦾
    public static final String SYMBOL_CP_COMPLETE = "\u29bf";       // ⦿

    public static final long TICK_RATE = 10;

    @Getter private final Region region;
    @Getter private final RegionSave regionSave;
    @Getter private final ControlPointService controlPointService;

    @Getter
    private final Set<Player> playersOnPoint = Sets.newHashSet();

    @Getter
    private MatchTeam controller = null;

    @Getter private int progress = 0;
    @Getter private final int progressToCap;
    @Getter private MatchTeam progressingTowardsTeam = null;

    @Getter private int runnableId = -1;

    public ControlPoint(Region region, int progressToCap, ControlPointService controlPointService, ControlPointService blockDisplayController) {
        this.region = region;
        this.progressToCap = progressToCap;
        this.controlPointService = controlPointService;

        regionSave = new RegionSave(region);
        renderBlocks();
    }

    private void handlePlayerMove(Player player, Location to) {
        if(TGM.get().getModule(SpectatorModule.class).isSpectating(player)) return;

        if (!player.isDead() && region.contains(to)) {
            playersOnPoint.add(player);
        } else {
            playersOnPoint.remove(player);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        handlePlayerMove(event.getPlayer(), event.getTo());
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        this.playersOnPoint.remove(event.getPlayerContext().getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.playersOnPoint.remove(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        this.playersOnPoint.remove(event.getPlayer());
    }

    public void enable() {
        runnableId = Bukkit.getScheduler().scheduleSyncRepeatingTask(TGM.get(), new Runnable() {
            @Override
            public void run() {
                HashMap<MatchTeam, Integer> holding = new HashMap<>();
                for (MatchTeam matchTeam : TGM.get().getModule(TeamManagerModule.class).getTeams()) {
                    if(matchTeam.isSpectator()) continue;

                    for (Player player : playersOnPoint) {
                        if (matchTeam.containsPlayer(player)) {
                            holding.put(matchTeam, holding.getOrDefault(matchTeam, 0) + 1);
                        }
                    }
                }

                List<MatchTeam> most = new ArrayList<>();
                for (MatchTeam matchTeam : holding.keySet()) {
                    if (most.isEmpty()) {
                        most.add(matchTeam);
                    } else {
                        if (holding.get(matchTeam) >= holding.get(most)) {
                            most.add(matchTeam);
                        }
                    }
                }

                if (most.size() == 1) {
                    handleCap(most.get(0));
                }
            }
        }, TICK_RATE, TICK_RATE);

        TGM.registerEvents(this);
    }

    private void handleCap(MatchTeam matchTeam) {
        if (progressingTowardsTeam == null) { //switch from neutral to progressing
            progressingTowardsTeam = matchTeam;
            progress++;
            controlPointService.capturing(matchTeam, progress, progressToCap, true);
        } else {
            if (matchTeam == progressingTowardsTeam) {
                if(progress < progressToCap) {
                    progress++; //don't go over the max cap number.
                    controlPointService.capturing(matchTeam, progress, progressToCap, true);
                }
            } else {
                progress--;
                controlPointService.capturing(matchTeam, progress, progressToCap, false);
            }

            if (progress == 0) {
                progressingTowardsTeam = matchTeam; //change directions

                if (controller != null) {
                    controlPointService.lost(controller);
                    controller = null;
                }
            } else if (progress >= progressToCap && matchTeam == progressingTowardsTeam) {
                if (controller == null) {
                    controller = matchTeam;
                    controlPointService.captured(matchTeam);
                } else {
                    controlPointService.holding(matchTeam);
                }
            }
        }

        renderBlocks();
    }

    private void renderBlocks() {
        byte color1 = progressingTowardsTeam != null ? ColorConverter.convertChatColorToDyeColor(progressingTowardsTeam.getColor()).getWoolData() : -1;
        byte color2 = controller != null ? ColorConverter.convertChatColorToDyeColor(controller.getColor()).getWoolData() : -1;
        Location center = region.getCenter();
        double x = center.getX();
        double y = center.getY();
        double percent = Math.toRadians((double) progress / (double) progressToCap * 3.6);
        for(Block block : region.getBlocks()) {
            if(!Blocks.isVisualMaterial(block.getType())) continue;
            double dx = block.getX() - x;
            double dy = block.getY() - y;
            double angle = Math.atan2(dy, dx);
            if(angle < 0) angle += 2 * Math.PI;
            byte color = angle < percent ? color1 : color2;
            if (color == -1) {
                Pair<Material,Byte> oldBlock = regionSave.getBlockAt(new BlockVector(block.getLocation().toVector()));
                if (oldBlock.getLeft().equals(block.getType())) color = oldBlock.getRight();
            }
            if (color != -1) {
                block.setData(color);
//                Bukkit.broadcastMessage("set to " + color);
            } else {
//                Bukkit.broadcastMessage("color = -1");
            }
        }
    }

    public void unload() {
        Bukkit.getScheduler().cancelTask(runnableId);
        HandlerList.unregisterAll(this);
    }
}
