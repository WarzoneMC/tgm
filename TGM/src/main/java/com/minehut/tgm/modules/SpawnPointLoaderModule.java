package com.minehut.tgm.modules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.minehut.tgm.TGM;
import com.minehut.tgm.map.SpawnPoint;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.match.ModuleData;
import com.minehut.tgm.match.ModuleLoadTime;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.util.Parser;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class SpawnPointLoaderModule extends MatchModule {

    @Override
    public void load(Match match) {
        parseTeamSpawns(match);
    }

    private void parseTeamSpawns(Match match) {
        JsonArray jsonArray = match.getMapContainer().getMapInfo().getJsonObject().getAsJsonArray("spawns");
        for (JsonElement spawnElement : jsonArray) {
            JsonObject spawnJson = spawnElement.getAsJsonObject();

            List<MatchTeam> teams = new ArrayList<>();
            for (Object o : spawnJson.getAsJsonArray("teams")) {
                String teamId = ((JsonPrimitive) o).getAsString();
                MatchTeam team = match.getModule(TeamManagerModule.class).getTeamById(teamId);
                if (team != null) {
                    teams.add(team);
                }
            }
            Location location = Parser.convertLocation(match.getWorld(), spawnJson);
            SpawnPoint spawnPoint = new SpawnPoint(location);
            for (MatchTeam matchTeam : teams) {
                Bukkit.getLogger().info("Added spawnpoint for " + matchTeam.getAlias());
                matchTeam.addSpawnPoint(spawnPoint);
            }
        }
    }
}
