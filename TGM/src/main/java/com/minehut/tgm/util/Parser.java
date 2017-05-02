package com.minehut.tgm.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minehut.tgm.modules.region.Region;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    public static Location convertLocation(World world, JsonObject locationJson) {
        double x = locationJson.get("x").getAsDouble();
        double y = locationJson.get("y").getAsDouble();
        double z = locationJson.get("z").getAsDouble();
        float yaw = 0;
        if (locationJson.has("yaw")) {
            yaw = locationJson.get("yaw").getAsFloat();
        }
        float pitch = 0;
        if (locationJson.has("pitch")) {
            pitch = locationJson.get("pitch").getAsFloat();
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static List<MatchTeam> getTeamsFromElement(TeamManagerModule teamManagerModule, JsonElement element) {
        List<MatchTeam> teams = new ArrayList<>();

        if (element.isJsonPrimitive()) {
            if (element.getAsString().equalsIgnoreCase("all")) {
                for (MatchTeam matchTeam : teamManagerModule.getTeams()) {
                    if (!matchTeam.isSpectator()) {
                        teams.add(matchTeam);
                    }
                }
            }
        } else {
            for (JsonElement jsonElement : element.getAsJsonArray()) {
                MatchTeam matchTeam = teamManagerModule.getTeamById(jsonElement.getAsString());
                if (matchTeam != null) {
                    teams.add(matchTeam);
                }
            }
        }

        return teams;
    }
}
