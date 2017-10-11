package network.warzone.tgm.modules.points;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.ModuleData;
import network.warzone.tgm.match.ModuleLoadTime;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ModuleData(load = ModuleLoadTime.EARLIEST)
public class PointsModule extends MatchModule {
    @Getter private final HashMap<MatchTeam, Integer> points = new HashMap<>();

    //amount of points the team has to reach to win the game.
    @Getter private final HashMap<MatchTeam, Integer> targets = new HashMap<>();

    @Getter private final List<PointService> services = new ArrayList<>();

    @Override
    public void load(Match match) {
        if (match.getMapContainer().getMapInfo().getJsonObject().has("points")) {
            JsonObject pointsJson = match.getMapContainer().getMapInfo().getJsonObject().get("points").getAsJsonObject();
            JsonElement targetElement = pointsJson.get("target");
            if (targetElement.isJsonPrimitive()) {
                int target = targetElement.getAsInt();
                for (MatchTeam matchTeam : TGM.get().getModule(TeamManagerModule.class).getTeams()) {
                    if (!matchTeam.isSpectator()) {
                        targets.put(matchTeam, target);
                    }
                }
            } else {
                //todo: per team target parsing
            }
        }
    }

    public void incrementPoints(MatchTeam matchTeam, int amount) {
        int updated = points.getOrDefault(matchTeam, 0) + amount;
        this.points.put(matchTeam, updated);

        if (updated >= targets.get(matchTeam)) {
            for (PointService pointService : services) {
                pointService.reachedTarget(matchTeam);
            }
        }
    }

    public int getPoints(MatchTeam matchTeam) {
        return this.points.getOrDefault(matchTeam, 0);
    }

    public int getTarget(MatchTeam matchTeam) {
        return this.targets.getOrDefault(matchTeam, 0);
    }

    public void addService(PointService pointService) {
        services.add(pointService);
    }
}
