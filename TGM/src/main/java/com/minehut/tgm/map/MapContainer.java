package com.minehut.tgm.map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minehut.tgm.util.Parser;
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

    public void parseWorldDependentContent(World world) {
        parseLocations(world);
    }

    private void parseLocations(World world) {
        if(!mapInfo.getJsonObject().has("locations")) return;

        JsonArray jsonArray = mapInfo.getJsonObject().getAsJsonArray("locations");
        for (JsonElement locationElement : jsonArray) {
            JsonObject locationJson = locationElement.getAsJsonObject();
            String id = locationJson.get("id").getAsString();
            Location location = Parser.convertLocation(world, locationJson);

            locations.put(id, location);
        }
    }
}
