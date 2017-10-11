package network.warzone.tgm.modules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.map.SpawnPoint;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.util.Parser;
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
            for (JsonElement o : spawnJson.getAsJsonArray("teams")) {
                String teamId = o.getAsString();
                MatchTeam team = match.getModule(TeamManagerModule.class).getTeamById(teamId);
                if (team != null) {
                    teams.add(team);
                }
            }

            Location location;
            if (spawnJson.has("coords")) {
                location = Parser.convertLocation(match.getWorld(), spawnJson.get("coords"));
            } else {
                location = Parser.convertLocation(match.getWorld(), spawnJson);
            }
            SpawnPoint spawnPoint = new SpawnPoint(location);
            for (MatchTeam matchTeam : teams) {
                matchTeam.addSpawnPoint(spawnPoint);
            }
        }
    }
}
