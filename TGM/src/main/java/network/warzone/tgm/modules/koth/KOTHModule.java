package network.warzone.tgm.modules.koth;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.controlpoint.ControlPoint;
import network.warzone.tgm.modules.controlpoint.ControlPointDefinition;
import network.warzone.tgm.modules.points.PointsModule;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.region.RegionManagerModule;
import network.warzone.tgm.modules.scoreboard.ScoreboardInitEvent;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.scoreboard.SimpleScoreboard;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.time.TimeModule;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class KOTHModule extends MatchModule implements Listener {

    private final List<ControlPoint> controlPoints = new ArrayList<>();
    private PointsModule pointsModule;

    private final HashMap<ControlPointDefinition, Integer> controlPointScoreboardLines = new HashMap<>();

    private final HashMap<MatchTeam, Integer> teamScoreboardLines = new HashMap<>();

    @Override
    public void load(Match match) {
        JsonObject kothJson = match.getMapContainer().getMapInfo().getJsonObject().get("koth").getAsJsonObject();

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

        pointsModule = match.getModule(PointsModule.class);
        pointsModule.addService(matchTeam -> TGM.get().getMatchManager().endMatch(matchTeam));

        TGM.get().getModule(TimeModule.class).setTimeLimitService(() -> getHighestPointsTeam());
    }

    private MatchTeam getHighestPointsTeam() {
        Map.Entry<MatchTeam, Integer> highest = null;
        for (Map.Entry<MatchTeam, Integer> entry : pointsModule.getPoints().entrySet()) {
            if (highest == null) {
                highest = entry;
                continue;
            }
            if (entry.getValue() > highest.getValue()) {
                highest = entry;
            }
        }
        if (highest != null) {
            final Map.Entry<MatchTeam, Integer> entry = highest;
            int amount = pointsModule.getPoints().entrySet().stream().filter(en -> entry.getValue() == en.getValue()).collect(Collectors.toList()).size();
            if (amount > 1) return null;
            else return entry.getKey();
        }
        return null;
    }


    public void incrementPoints(MatchTeam matchTeam, int amount) {
        pointsModule.incrementPoints(matchTeam, amount);
        updateScoreboardTeamLine(matchTeam);
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
            if (controlPoint.getDefinition() == definition) return controlPoint;
        }
        return null;
    }

    @EventHandler
    public void onScoreboardInit(ScoreboardInitEvent event) {
        List<MatchTeam> teams = TGM.get().getModule(TeamManagerModule.class).getTeams();
        SimpleScoreboard simpleScoreboard = event.getSimpleScoreboard();

        int i;
        for (i = 0; i < controlPoints.size(); i++) {
            ControlPoint controlPoint = controlPoints.get(i);

            controlPointScoreboardLines.put(controlPoint.getDefinition(), i);
            simpleScoreboard.add(getControlPointScoreboardLine(controlPoint), i);
        }

        i++;
        simpleScoreboard.add(" ", i);

        i++;
        for (MatchTeam matchTeam : teams) {
            if (matchTeam.isSpectator()) continue;

            simpleScoreboard.add(getTeamScoreLine(matchTeam), i);
            teamScoreboardLines.put(matchTeam, i);

            i++;
        }
    }

    private String getTeamScoreLine(MatchTeam matchTeam) {
        return pointsModule.getPoints(matchTeam) + ChatColor.DARK_GRAY.toString() + "/" + ChatColor.GRAY.toString() + pointsModule.getTarget(matchTeam) + " " + matchTeam.getColor() + matchTeam.getAlias();
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

        controlPoints.clear();
        controlPointScoreboardLines.clear();
        teamScoreboardLines.clear();
    }
}
