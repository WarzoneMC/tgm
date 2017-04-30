package com.minehut.tgm.modules.koth;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minehut.tgm.TGM;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.controlpoint.ControlPoint;
import com.minehut.tgm.modules.controlpoint.ControlPointService;
import com.minehut.tgm.modules.region.Region;
import com.minehut.tgm.modules.region.RegionManagerModule;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import com.minehut.tgm.util.Parser;
import com.sk89q.minecraft.util.commands.ChatColor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KOTHModule extends MatchModule implements Listener {
    @Getter private final List<ControlPoint> controlPoints = new ArrayList<>();

    @Getter private int pointsToWin;

    @Getter
    private final HashMap<MatchTeam, Integer> points = new HashMap<>();

    @Override
    public void load(Match match) {
        JsonObject kothJson = match.getMapContainer().getMapInfo().getJsonObject().get("koth").getAsJsonObject();
        pointsToWin = kothJson.get("points").getAsInt();

        for (JsonElement capturePointElement : kothJson.getAsJsonArray("hills")) {
            JsonObject capturePointJson = capturePointElement.getAsJsonObject();
            Region region = match.getModule(RegionManagerModule.class).getRegion(match, capturePointJson.get("region"));
            int timeToCap = 10;
            if (capturePointJson.has("time")) {
                timeToCap = capturePointJson.get("time").getAsInt();
            }

            int swap = 1;
            if (capturePointJson.has("points")) {
                swap = capturePointJson.get("points").getAsInt();
            }
            final int pointsPerHold = swap;

            final String name = capturePointJson.get("name").getAsString();

            controlPoints.add(new ControlPoint(region, timeToCap, new ControlPointService() {
                @Override
                public void holding(MatchTeam matchTeam) {
                    incrementPoints(matchTeam);
                }

                @Override
                public void capturing(MatchTeam matchTeam, int progress, int maxProgress, boolean upward) {
                    Bukkit.broadcastMessage(matchTeam.getAlias() + " is capturing " + name + " (" + progress + "/" + maxProgress + ")");
                }

                @Override
                public void captured(MatchTeam matchTeam) {
                    Bukkit.broadcastMessage(matchTeam.getColor() + ChatColor.BOLD.toString() + matchTeam.getAlias() + ChatColor.WHITE
                            + " took control of " + ChatColor.AQUA + ChatColor.BOLD.toString() + name);

                    if (incrementPoints(matchTeam)) {
                        return; //don't play capture sound if the game ends. There already is an endgame sound.
                    }

                    for (MatchTeam team : match.getModule(TeamManagerModule.class).getTeams()) {
                        for (PlayerContext playerContext : team.getMembers()) {
                            if (team == matchTeam || team.isSpectator()) {
                                playerContext.getPlayer().playSound(playerContext.getPlayer().getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.7f, 2f);
                            } else {
                                playerContext.getPlayer().playSound(playerContext.getPlayer().getLocation(), Sound.ENTITY_BLAZE_DEATH, 0.8f, 0.8f);
                            }
                        }
                    }
                }

                @Override
                public void lost(MatchTeam matchTeam) {
                    Bukkit.broadcastMessage(matchTeam.getColor() + ChatColor.BOLD.toString() + matchTeam.getAlias() + ChatColor.WHITE
                            + " lost control of " + ChatColor.AQUA + ChatColor.BOLD.toString() + name);
                }

                //returns true if winner was called
                private boolean incrementPoints(MatchTeam matchTeam) {
                    points.put(matchTeam, points.getOrDefault(matchTeam, 0) + pointsPerHold);

                    if (points.get(matchTeam) >= pointsToWin) {
                        TGM.get().getMatchManager().endMatch(matchTeam);
                        return true;
                    }
                    return false;
                }
            }, null));

            for (ControlPoint controlPoint : controlPoints) {
                controlPoint.enable();
            }
        }
    }

    @Override
    public void disable() {
        for (ControlPoint controlPoint : controlPoints) {
            controlPoint.unload();
        }
    }

    @Override
    public void unload() {
        for (ControlPoint controlPoint : controlPoints) {
            controlPoint.unload();
        }
    }
}
