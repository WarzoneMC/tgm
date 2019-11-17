package network.warzone.tgm.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    public static Location convertLocation(World world, JsonElement locationElement) {
        if (locationElement.isJsonObject()) {
            JsonObject locationJson = locationElement.getAsJsonObject();

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
        } else {
            String[] split = locationElement.getAsString().replaceAll(" ", "").split(",");

            double x = Double.parseDouble(split[0].replaceAll("oo", Integer.toString(Integer.MAX_VALUE)));
            double y = Double.parseDouble(split[1].replaceAll("oo", Integer.toString(Integer.MAX_VALUE)));
            double z = Double.parseDouble(split[2].replaceAll("oo", Integer.toString(Integer.MAX_VALUE)));

            float yaw = 0;
            float pitch = 0;

            if (split.length >= 4) {
                yaw = Float.parseFloat(split[3].replaceAll("oo", Integer.toString(Integer.MAX_VALUE)));
            }

            if (split.length >= 5) {
                pitch = Float.parseFloat(split[4].replaceAll("oo", Integer.toString(Integer.MAX_VALUE)));
            }

            return new Location(world, x, y, z, yaw, pitch);
        }
    }

    public static Vector convertVector(JsonElement vectorElement) {
        if (vectorElement.isJsonObject()) {
            JsonObject vectorJson = vectorElement.getAsJsonObject();
            double x = vectorJson.get("x").getAsDouble();
            double y = vectorJson.get("y").getAsDouble();
            double z = vectorJson.get("z").getAsDouble();
            return new Vector(x, y, z);
        } else {
            String[] split = vectorElement.getAsString().replaceAll(" ", "").split(",");
            double x = Double.parseDouble(split[0].replaceAll("oo", Integer.toString(Integer.MAX_VALUE)));
            double y = Double.parseDouble(split[1].replaceAll("oo", Integer.toString(Integer.MAX_VALUE)));
            double z = Double.parseDouble(split[2].replaceAll("oo", Integer.toString(Integer.MAX_VALUE)));
            return new Vector(x, y, z);
        }
    }

    public static List<MatchTeam> getTeamsFromElement(TeamManagerModule teamManagerModule, JsonElement element) {
        List<MatchTeam> teams = new ArrayList<>();

        if (element.isJsonArray()) {
            for (JsonElement jsonElement : element.getAsJsonArray()) {
                MatchTeam matchTeam = teamManagerModule.getTeamById(jsonElement.getAsString());
                if (matchTeam != null) {
                    teams.add(matchTeam);
                }
            }
        }

        return teams;
    }

    /**
     * returns null if all materials are allowed
     */
    public static List<Material> getMaterialsFromElement(JsonElement element) {
        List<Material> materials = new ArrayList<>();

        if (element.isJsonPrimitive()) {
            if (element.getAsString().equalsIgnoreCase("all")) {
                return null;
            }
        } else {
            for (JsonElement jsonElement : element.getAsJsonArray()) {
                materials.add(Material.valueOf(Strings.getTechnicalName(jsonElement.getAsString())));
            }
        }

        return materials;
    }
}
