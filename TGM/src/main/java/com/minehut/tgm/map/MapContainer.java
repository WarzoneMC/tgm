package com.minehut.tgm.map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.util.HashMap;

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

    public void parseLocations(World world) {
        JsonArray jsonArray = mapInfo.getJsonObject().getAsJsonArray("locations");
        for (JsonElement locationElement : jsonArray) {
            JsonObject locationJson = locationElement.getAsJsonObject();
            String id = locationJson.get("id").getAsString();
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

            locations.put(id, new Location(world, x, y, z, yaw, pitch));
        }
    }
}
