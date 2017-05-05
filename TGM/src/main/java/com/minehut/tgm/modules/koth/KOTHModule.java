package com.minehut.tgm.modules.koth;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minehut.tgm.TGM;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.controlpoint.ControlPoint;
import com.minehut.tgm.modules.controlpoint.ControlPointDefinition;
import com.minehut.tgm.modules.controlpoint.ControlPointService;
import com.minehut.tgm.modules.region.Region;
import com.minehut.tgm.modules.region.RegionManagerModule;
import com.minehut.tgm.modules.scoreboard.ScoreboardInitEvent;
import com.minehut.tgm.modules.scoreboard.ScoreboardManagerModule;
import com.minehut.tgm.modules.scoreboard.SimpleScoreboard;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KOTHModule extends MatchModule implements Listener {
    @Getter private final List<ControlPoint> controlPoints = new ArrayList<>();

    @Getter private int pointsToWin;

    @Getter
    private final HashMap<MatchTeam, Integer> points = new HashMap<>();

    @Getter
    private final HashMap<ControlPointDefinition, Integer> controlPointScoreboardLines = new HashMap<>();

    @Getter
    private final HashMap<MatchTeam, Integer> teamScoreboardLines = new HashMap<>();

    @Override
    public void load(Match match) {
        JsonObject kothJson = match.getMapContainer().getMapInfo().getJsonObject().get("koth").getAsJsonObject();
        pointsToWin = kothJson.get("points").getAsInt();

        int scoreboardLine = 1;

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

            ControlPointDefinition definition = new ControlPointDefinition(name, timeToCap, pointsPerHold);
            ControlPoint controlPoint = new ControlPoint(definition, region, new KOTHControlPointService(this, match, definition));

            controlPoints.add(controlPoint);
        }

        for (ControlPoint controlPoint : controlPoints) {
            controlPoint.enable();
        }
    }


    //returns true if winner was called
    public boolean incrementPoints(MatchTeam matchTeam, int amount) {
        points.put(matchTeam, points.getOrDefault(matchTeam, 0) + amount);

        updateScoreboardTeamLine(matchTeam);

        if (points.get(matchTeam) >= pointsToWin) {
            TGM.get().getMatchManager().endMatch(matchTeam);
            return true;
        }
        return false;
    }

    public void updateScoreboardTeamLine(MatchTeam matchTeam) {
        for (SimpleScoreboard simpleScoreboard : TGM.get().getModule(ScoreboardManagerModule.class).getScoreboards().values()) {
            int line = teamScoreboardLines.get(matchTeam);
            simpleScoreboard.remove(line);
            simpleScoreboard.add(getTeamScoreLine(matchTeam), line);
            simpleScoreboard.update();
        }
    }

    public void updateScoreboardControlPointLine(ControlPointDefinition definition) {
        for (SimpleScoreboard simpleScoreboard : TGM.get().getModule(ScoreboardManagerModule.class).getScoreboards().values()) {
            int line = controlPointScoreboardLines.get(definition);
            simpleScoreboard.remove(line);
            simpleScoreboard.add(getControlPointScoreboardLine(getControlPointByDefinition(definition)), line);
            simpleScoreboard.update();
        }
    }

    public ControlPoint getControlPointByDefinition(ControlPointDefinition definition) {
        for (ControlPoint controlPoint : controlPoints) {
            if(controlPoint.getDefinition() == definition) return controlPoint;
        }
        return null;
    }

    @EventHandler
    public void onScoreboardInit(ScoreboardInitEvent event) {
        List<MatchTeam> teams = TGM.get().getModule(TeamManagerModule.class).getTeams();


        SimpleScoreboard simpleScoreboard = event.getSimpleScoreboard();

        int i;
        for(i = 0; i < controlPoints.size(); i++) {
            ControlPoint controlPoint = controlPoints.get(i);

            controlPointScoreboardLines.put(controlPoint.getDefinition(), i);
            simpleScoreboard.add(getControlPointScoreboardLine(controlPoint), i);
        }

        i++;
        simpleScoreboard.add(" ", i);

        i++;
        for (MatchTeam matchTeam : teams) {
            if(matchTeam.isSpectator()) continue;

            simpleScoreboard.add(getTeamScoreLine(matchTeam), i);
            teamScoreboardLines.put(matchTeam, i);

            i++;
        }
    }

    private String getTeamScoreLine(MatchTeam matchTeam) {
        return points.getOrDefault(matchTeam, 0) + ChatColor.DARK_GRAY.toString() + "/" + ChatColor.GRAY.toString() + pointsToWin + " " + matchTeam.getColor() + matchTeam.getAlias();
    }

    private String getControlPointScoreboardLine(ControlPoint controlPoint) {
        if (controlPoint.isInProgress()) {
            if (controlPoint.getController() == null) {
                return controlPoint.getProgressingTowardsTeam().getColor().toString() + controlPoint.getPercent() + "% " + ChatColor.WHITE + controlPoint.getDefinition().getName();
            } else {
                return controlPoint.getPercent() + "% " + controlPoint.getController().getColor() + controlPoint.getDefinition().getName();
            }
        } else {
            if (controlPoint.getController() == null) {
                return ControlPoint.SYMBOL_CP_INCOMPLETE + " " + controlPoint.getDefinition().getName();
            } else {
                return ControlPoint.SYMBOL_CP_COMPLETE + " " + controlPoint.getController().getColor() + controlPoint.getDefinition().getName();
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
