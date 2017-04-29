package com.minehut.tgm.map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.minehut.tgm.TGM;
import com.minehut.tgm.team.MatchTeam;
import com.minehut.tgm.team.TeamManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Serves as the "anchor" for maps.
 * This allows map information to be easily reloaded
 * during runtime.
 */
@AllArgsConstructor
public class MapContainer {
    @Getter private File sourceFolder;
    @Getter @Setter private MapInfo mapInfo;

    @Getter
    private final HashMap<String, Location> locations = new HashMap<>();

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

    public void parseWorldDependentContent(World world) {
        parseLocations(world);
        parseTeamSpawns(world);
    }

    private void parseTeamSpawns(World world) {
        JsonArray jsonArray = mapInfo.getJsonObject().getAsJsonArray("spawns");
        for (JsonElement spawnElement : jsonArray) {
            JsonObject spawnJson = spawnElement.getAsJsonObject();

            List<MatchTeam> teams = new ArrayList<>();
            for (Object o : spawnJson.getAsJsonArray("teams")) {
                String teamId = ((JsonPrimitive) o).getAsString();
                MatchTeam team = TGM.getTgm().getTeamManager().getTeam(teamId);
                if (team != null) {
                    teams.add(team);
                }
            }
            Location location = convertLocation(world, spawnJson);
            SpawnPoint spawnPoint = new SpawnPoint(location);
            for (MatchTeam matchTeam : teams) {
                Bukkit.getLogger().info("Added spawnpoint for " + matchTeam.getAlias());
                matchTeam.addSpawnPoint(spawnPoint);
            }
        }
    }

    private void parseLocations(World world) {
        JsonArray jsonArray = mapInfo.getJsonObject().getAsJsonArray("locations");
        for (JsonElement locationElement : jsonArray) {
            JsonObject locationJson = locationElement.getAsJsonObject();
            String id = locationJson.get("id").getAsString();
            Location location = convertLocation(world, locationJson);

            locations.put(id, location);
        }
    }
}
