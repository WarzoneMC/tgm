package com.minehut.tgm.modules.controlpoint;

import com.google.common.collect.Sets;
import com.minehut.tgm.TGM;
import com.minehut.tgm.modules.SpectatorModule;
import com.minehut.tgm.modules.region.Region;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
    @Getter private final ControlPointService controlPointService;
    @Getter private final ControlPointService blockDisplayController;

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

        if (blockDisplayController != null) {
            this.blockDisplayController = blockDisplayController;
        } else {
            this.blockDisplayController = new BlockDisplayControllerImpl();
        }
    }

    private void handlePlayerMove(Player player, Location to) {
        if(TGM.get().getModule(SpectatorModule.class).isSpectating(player)) return;

        if (player.isDead() && region.contains(to)) {
            playersOnPoint.add(player);
        } else {
            playersOnPoint.remove(player);
        }
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
                    if (most == null) {
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
    }

    private void handleCap(MatchTeam matchTeam) {
        if (progressingTowardsTeam == null) { //switch from neutral to progressing
            progressingTowardsTeam = matchTeam;
            progress++;
            callServiceCapturing(matchTeam, progress, progressToCap, true);
        } else {
            if (matchTeam == progressingTowardsTeam) {
                if(progress < progressToCap) {
                    progress++; //don't go over the max cap number.
                    callServiceCapturing(matchTeam, progress, progressToCap, true);
                }
            } else {
                progress--;
                callServiceCapturing(matchTeam, progress, progressToCap, false);
            }

            if (progress == 0) {
                progressingTowardsTeam = matchTeam; //change directions
                controller = null;
                callServiceLost(matchTeam);
            } else if (progress >= progressToCap && matchTeam == progressingTowardsTeam) {
                if (controller == null || controller != matchTeam) {
                    callServiceLost(matchTeam);
                    controller = matchTeam;
                    callServiceCaptured(matchTeam);
                } else {
                    callServiceHolding(matchTeam);
                }
            }
        }
    }

    private void callServiceCaptured(MatchTeam matchTeam) {
        controlPointService.captured(matchTeam);
        blockDisplayController.captured(matchTeam);
    }

    private void callServiceLost(MatchTeam matchTeam) {
        controlPointService.lost(matchTeam);
        blockDisplayController.lost(matchTeam);
    }

    private void callServiceHolding(MatchTeam matchTeam) {
        controlPointService.holding(matchTeam);
        blockDisplayController.holding(matchTeam);
    }

    private void callServiceCapturing(MatchTeam matchTeam, int progress, int maxProgress, boolean upward) {
        controlPointService.capturing(matchTeam, progress, maxProgress, upward);
        blockDisplayController.capturing(matchTeam, progress, maxProgress, upward);
    }

    public void unload() {
        Bukkit.getScheduler().cancelTask(runnableId);
        HandlerList.unregisterAll(this);
    }
}
