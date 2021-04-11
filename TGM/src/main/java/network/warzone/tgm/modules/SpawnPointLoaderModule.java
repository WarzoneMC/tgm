package network.warzone.tgm.modules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.map.spawnpoints.AbsoluteRegionSpawnPoint;
import network.warzone.tgm.map.spawnpoints.LocationSpawnPoint;
import network.warzone.tgm.map.spawnpoints.RelativeRegionSpawnPoint;
import network.warzone.tgm.map.spawnpoints.SpawnPoint;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.region.RegionManagerModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.util.Parser;

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

            SpawnPoint spawnPoint = null;
            if (spawnJson.has("coords")) {
                spawnPoint = new LocationSpawnPoint(Parser.convertLocation(match.getWorld(), spawnJson.get("coords")));
            } else if (spawnJson.has("region")) {
                Region region = match.getModule(RegionManagerModule.class).getRegion(match, spawnJson.getAsJsonObject("region"));

                if (spawnJson.has("face-coordinates")) {
                    spawnPoint = new RelativeRegionSpawnPoint(region, Parser.convertLocation(match.getWorld(), spawnJson.get("face-coordinates")));
                } else {
                    float yaw = 0; float pitch = 0;
                    if (spawnJson.has("yaw")) yaw = spawnJson.get("yaw").getAsFloat();
                    if (spawnJson.has("pitch")) pitch = spawnJson.get("pitch").getAsFloat();

                    spawnPoint = new AbsoluteRegionSpawnPoint(region, yaw, pitch);
                }
            }

            for (MatchTeam matchTeam : teams) {
                if (spawnPoint == null) continue;
                matchTeam.addSpawnPoint(spawnPoint);
            }
        }
    }
}
