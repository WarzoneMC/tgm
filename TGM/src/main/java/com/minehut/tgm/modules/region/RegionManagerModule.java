package com.minehut.tgm.modules.region;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.match.ModuleData;
import com.minehut.tgm.match.ModuleLoadTime;
import com.minehut.tgm.util.Parser;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashMap;

@ModuleData(load = ModuleLoadTime.EARLIEST)
public class RegionManagerModule extends MatchModule {
    @Getter private final HashMap<String, Region> regions = new HashMap<>();

    @Override
    public void load(Match match) {
        regions.put("global", new CuboidRegion(match.getWorld(),
                new Location(match.getWorld(), Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE),
                new Location(match.getWorld(), Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE)));

        if (match.getMapContainer().getMapInfo().getJsonObject().has("regions")) {
            for (JsonElement regionElement : match.getMapContainer().getMapInfo().getJsonObject().getAsJsonArray("regions")) {
                getRegion(match, regionElement);
            }
        }
    }

    /**
     * Can pass in either the region id as a String or the
     * region specification json as a Json Object.
     *
     * If the Json Object contains an "id" attribute, it will
     * be saved in the global regions hashmap.
     */
    public Region getRegion(Match match, JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            return regions.get(jsonElement.getAsString());
        } else {
            JsonObject regionJson = jsonElement.getAsJsonObject();
            Region region = null;
            String type = "cuboid";

            if (regionJson.has("type")) {
                type = regionJson.get("type").getAsString().toLowerCase();
            }

            switch (type) {
                case "cylinder":
                    region = new CylinderRegion(
                            Parser.convertLocation(match.getWorld(), regionJson.get("base")),
                            regionJson.get("radius").getAsDouble(),
                            regionJson.get("height").getAsDouble()
                    );
                    break;
                case "cuboid":
                default:
                    region = new CuboidRegion(
                            match.getWorld(),
                            Parser.convertLocation(match.getWorld(), regionJson.get("min")),
                            Parser.convertLocation(match.getWorld(), regionJson.get("max"))
                    );
                    break;
            }

            if (region != null && regionJson.has("id")) {
                regions.put(regionJson.get("id").getAsString(), region);
            }

            return region;
        }
    }

}
