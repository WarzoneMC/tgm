package com.minehut.tgm.modules.points;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minehut.tgm.TGM;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.match.ModuleData;
import com.minehut.tgm.match.ModuleLoadTime;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import it.unimi.dsi.fastutil.Hash;
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
        int updated = this.points.put(matchTeam, points.getOrDefault(matchTeam, 0) + amount);

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
