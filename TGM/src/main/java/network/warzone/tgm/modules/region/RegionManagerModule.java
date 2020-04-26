package network.warzone.tgm.modules.region;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.ModuleData;
import network.warzone.tgm.match.ModuleLoadTime;
import network.warzone.tgm.util.Parser;
import org.bukkit.Location;

import java.util.HashMap;

@ModuleData(load = ModuleLoadTime.EARLIEST) @Getter
public class RegionManagerModule extends MatchModule {

    private final HashMap<String, Region> regions = new HashMap<>();

    @Override
    public void load(Match match) {
        regions.put("global", new CuboidRegion(
                new Location(match.getWorld(), Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE),
                new Location(match.getWorld(), Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE))
        );

        if (match.getMapContainer().getMapInfo().getJsonObject().has("regions")) {
            for (JsonElement regionElement : match.getMapContainer().getMapInfo().getJsonObject().getAsJsonArray("regions")) {
                getRegion(match, regionElement);
            }
        }
    }

    @Override
    public void unload() {
        regions.clear();
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
            Region region;
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
                case "sphere":
                    region = new SphereRegion(
                            Parser.convertLocation(match.getWorld(), regionJson.get("center")),
                            regionJson.get("radius").getAsDouble()
                    );
                    break;
                case "hemisphere":
                    region = new HemisphereRegion(
                            Parser.convertLocation(match.getWorld(), regionJson.get("center")),
                            regionJson.get("radius").getAsDouble(),
                            regionJson.has("direction") ? HemisphereRegion.parseHemisphereDirection(regionJson.get("direction")) : HemisphereRegion.HemisphereFace.NEGATIVE_Z
                    );
                    break;
                case "meta":
                    region = new MetaRegion(regionJson.getAsJsonArray("regions"));
                    break;
                case "cuboid":
                default:
                    region = new CuboidRegion(
                            Parser.convertLocation(match.getWorld(), regionJson.get("min")),
                            Parser.convertLocation(match.getWorld(), regionJson.get("max"))
                    );
                    break;
            }

            if (regionJson.has("id")) {
                regions.put(regionJson.get("id").getAsString(), region);
            }

            return region;
        }
    }

}
