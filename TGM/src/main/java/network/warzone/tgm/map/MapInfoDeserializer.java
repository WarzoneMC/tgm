package network.warzone.tgm.map;

import com.google.gson.*;
import network.warzone.tgm.gametype.GameType;
import org.bukkit.ChatColor;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
public class MapInfoDeserializer implements JsonDeserializer<MapInfo> {
    @Override
    public MapInfo deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();
        String name = json.get("name").getAsString();
        String version = json.get("version").getAsString();
        List<String> authors = new ArrayList<>();
        for (JsonElement authorJson : json.getAsJsonArray("authors")) {
            authors.add(authorJson.getAsString());
        }
        GameType gameType = GameType.valueOf(json.get("gametype").getAsString());
        List<ParsedTeam> parsedTeams = new ArrayList<>();
        for (JsonElement teamElement : json.getAsJsonArray("teams")) {
            JsonObject teamJson = teamElement.getAsJsonObject();
            String teamId = teamJson.get("id").getAsString();
            String teamName = teamJson.get("name").getAsString();
            ChatColor teamColor = ChatColor.valueOf(teamJson.get("color").getAsString().toUpperCase().replace(" ", "_"));
            int teamMax = teamJson.get("max").getAsInt();
            int teamMin = teamJson.get("min").getAsInt();
            parsedTeams.add(new ParsedTeam(teamId, teamName, teamColor, teamMax, teamMin));
        }

        return new MapInfo(name, version, authors, gameType, parsedTeams, json);
    }
}
