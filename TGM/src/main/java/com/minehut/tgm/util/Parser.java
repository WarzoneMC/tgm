package com.minehut.tgm.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minehut.tgm.modules.region.Region;
import org.bukkit.Location;
import org.bukkit.World;

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
}
