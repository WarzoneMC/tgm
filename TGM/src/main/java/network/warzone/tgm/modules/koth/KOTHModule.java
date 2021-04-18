package network.warzone.tgm.modules.koth;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.controlpoint.ControlPoint;
import network.warzone.tgm.modules.controlpoint.ControlPointDefinition;
import network.warzone.tgm.modules.points.PointsModule;
import network.warzone.tgm.modules.portal.Portal;
import network.warzone.tgm.modules.portal.PortalManagerModule;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.region.RegionManagerModule;
import network.warzone.tgm.modules.scoreboard.ScoreboardInitEvent;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.scoreboard.SimpleScoreboard;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.team.event.TeamUpdateAliasEvent;
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
    private KOTHObjective kothObjective = KOTHObjective.POINTS;

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
            MatchTeam owner = null;
            if (capturePointJson.has("owner")) {
                owner = TGM.get().getModule(TeamManagerModule.class).getTeamById(capturePointJson.get("owner").getAsString());
            }
            final int pointsPerHold = swap;
            final String name = capturePointJson.get("name").getAsString();

            HashMap<MatchTeam, Portal> portals = null;
            if (capturePointJson.has("portals")) {
                portals = new HashMap<>();
                JsonObject jsonPortals = capturePointJson.getAsJsonObject("portals");

                for (MatchTeam matchTeam : TGM.get().getModule(TeamManagerModule.class).getTeamsParticipating()) {
                    if (jsonPortals.has(matchTeam.getId())) {
                        portals.put(matchTeam, TGM.get().getModule(PortalManagerModule.class).getPortal(match, jsonPortals.get(matchTeam.getId())));
                    }
                }
            }

            ControlPointDefinition definition = new ControlPointDefinition(name, owner, timeToCap, pointsPerHold, portals);
            ControlPoint controlPoint = new ControlPoint(this, definition, region, new KOTHControlPointService(this, match, definition));

            controlPoints.add(controlPoint);
        }

        for (ControlPoint controlPoint : controlPoints) {
            controlPoint.enable();
        }

        if (match.getMapContainer().getMapInfo().getJsonObject().has("points")) {
            kothObjective = KOTHObjective.POINTS;
            pointsModule = match.getModule(PointsModule.class);
            pointsModule.addService(matchTeam -> TGM.get().getMatchManager().endMatch(matchTeam));
            TGM.get().getModule(TimeModule.class).setTimeLimitService(this::getHighestPointsTeam);
        } else {
            Preconditions.checkArgument(TGM.get().getModule(TeamManagerModule.class).getTeams().size() == 3, "Capture Point gametype must have exactly 2 teams");
            kothObjective = KOTHObjective.CAPTURES;
            TGM.get().getModule(TimeModule.class).setTimeLimitService(this::getMostCapturesTeam);
        }
    }

    public boolean isCapturable(ControlPoint toCapture, MatchTeam matchTeam) {
        if (kothObjective != KOTHObjective.CAPTURES) return true;

        MatchTeam initialOwner = controlPoints.get(0).getDefinition().getInitialOwner();
        boolean reversed = matchTeam != initialOwner;

        int highestCapturePointIndex = -1;
        for (int i = 0; i < controlPoints.size(); i++) {
            int index = reversed ? controlPoints.size() - 1 - i : i;

            ControlPoint controlPoint = controlPoints.get(index);
            MatchTeam controller = controlPoint.getController();

            if (controller != matchTeam) {
                highestCapturePointIndex = index;
                break;
            }
        }

        int controlPointIndex = -1;
        for (int i = 0; i < controlPoints.size(); i++) {
            if (toCapture.equals(controlPoints.get(i))) {
                controlPointIndex = i;
                break;
            }
        }

        return highestCapturePointIndex == controlPointIndex;
    }

    private MatchTeam getMostCapturesTeam() {
        TeamManagerModule teamManagerModule = TGM.get().getModule(TeamManagerModule.class);

        HashMap<String, Integer> progress = new HashMap<>();

        for (int i = 1; i < teamManagerModule.getTeams().size(); i++) {
            progress.put(teamManagerModule.getTeams().get(i).getId(), 0);
        }

        for (ControlPoint controlPoint : controlPoints) {
            MatchTeam controller;

            if (controlPoint.getController() == null) {
                if (controlPoint.getProgressingTowardsTeam() == null) continue;
                if (controlPoint.getPercent() <= 0) continue;
                controller = controlPoint.getProgressingTowardsTeam();
            } else {
                controller = controlPoint.getController();
            }

            for (String id : progress.keySet()) {
                if (controller.getId().equalsIgnoreCase(id)) {
                    progress.put(id, progress.get(id) + controlPoint.getPercent());
                    break;
                }
            }
        }

        Map.Entry<String, Integer> highest = null;
        for (Map.Entry<String, Integer> entry : progress.entrySet()) {
            if (highest == null) {
                highest = entry;
                continue;
            }

            if (entry.getValue() > highest.getValue()) {
                highest = entry;
            }
        }

        if (highest != null) {
            final Map.Entry<String, Integer> entry = highest;
            int amount = (int) progress.entrySet().stream().filter(en -> entry.getValue().equals(en.getValue())).count();
            if (amount > 1) return null;
            else return TGM.get().getModule(TeamManagerModule.class).getTeamById(entry.getKey());
        }
        return null;
    }

    private MatchTeam getHighestPointsTeam() {
        Map.Entry<String, Integer> highest = null;
        for (Map.Entry<String, Integer> entry : pointsModule.getPoints().entrySet()) {
            if (highest == null) {
                highest = entry;
                continue;
            }
            if (entry.getValue() > highest.getValue()) {
                highest = entry;
            }
        }
        if (highest != null) {
            final Map.Entry<String, Integer> entry = highest;
            int amount = pointsModule.getPoints().entrySet().stream().filter(en -> entry.getValue() == en.getValue()).collect(Collectors.toList()).size();
            if (amount > 1) return null;
            else return TGM.get().getModule(TeamManagerModule.class).getTeamById(entry.getKey());
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

        if (kothObjective == KOTHObjective.POINTS) {
            simpleScoreboard.setTitle(ChatColor.AQUA + "King of the Hill");
        } else {
            simpleScoreboard.setTitle(ChatColor.AQUA + "Capture Points");
        }

        int i = 2;
        for (int k = controlPoints.size() - 1; k >= 0; k--) {
            ControlPoint controlPoint = controlPoints.get(k);
            controlPointScoreboardLines.put(controlPoint.getDefinition(), i);
            simpleScoreboard.add(getControlPointScoreboardLine(controlPoint), i);
            i++;
        }
        simpleScoreboard.add(" ", i);

        if (kothObjective == KOTHObjective.CAPTURES) return;

        for (int j = teams.size() - 1; j >= 0; j--) {
            MatchTeam matchTeam = teams.get(j);

            if (matchTeam.isSpectator()) continue;

            i++;

            simpleScoreboard.add(getTeamScoreLine(matchTeam), i);
            teamScoreboardLines.put(matchTeam, i);
        }
    }

    private String getTeamScoreLine(MatchTeam matchTeam) {
        return ChatColor.WHITE.toString() + pointsModule.getPoints(matchTeam) + ChatColor.DARK_GRAY.toString() + "/" + ChatColor.GRAY.toString() + pointsModule.getTarget(matchTeam) + " " + matchTeam.getColor() + matchTeam.getAlias();
    }

    private String getControlPointScoreboardLine(ControlPoint controlPoint) {
        if (controlPoint.isInProgress()) {
            if (controlPoint.getController() == null) {
                return controlPoint.getProgressingTowardsTeam().getColor().toString() + controlPoint.getPercent() + "% " + ChatColor.WHITE + controlPoint.getDefinition().getName();
            } else {
                return ChatColor.WHITE.toString() + controlPoint.getPercent() + "% " + controlPoint.getController().getColor() + controlPoint.getDefinition().getName();
            }
        } else {
            if (controlPoint.getController() == null) {
                return ChatColor.WHITE.toString() + ControlPoint.SYMBOL_CP_INCOMPLETE + " " + controlPoint.getDefinition().getName();
            } else {
                return ChatColor.WHITE.toString() + ControlPoint.SYMBOL_CP_COMPLETE + " " + controlPoint.getController().getColor() + controlPoint.getDefinition().getName();
            }
        }
    }

    @EventHandler
    public void onTeamUpdate(TeamUpdateAliasEvent event) {
        MatchTeam team = event.getMatchTeam();
        if (!team.isSpectator()) updateScoreboardTeamLine(team);
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
