package network.warzone.tgm.map;

import com.google.gson.*;
import network.warzone.tgm.TGM;
import network.warzone.tgm.gametype.GameType;
import network.warzone.warzoneapi.models.Author;
import network.warzone.warzoneapi.models.MojangProfile;
import org.bukkit.Bukkit;
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
        List<Author> authors = new ArrayList<>();
        for (JsonElement authorJson : json.getAsJsonArray("authors")) {
            if (authorJson.isJsonPrimitive()) {
                authors.add(new Author(authorJson.getAsString()));
            } else {
                Author author = TGM.get().getGson().fromJson(authorJson, Author.class);
                if (TGM.get().getConfig().getBoolean("map.get-names"))
                    Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                        if (author != null && author.getUuid() != null) {
                            try {
                                MojangProfile profile = TGM.get().getTeamClient().getMojangProfile(author.getUuid());
                                if (profile != null && profile.getCode() == 0)
                                    author.setDisplayUsername(profile.getUsername());
                            } catch (Exception e) {
                                TGM.get().getLogger().warning("Could not retrieve current name for " + author.getUuid().toString() + " on map " + name);
                            }
                        }
                    });
                authors.add(author);
            }
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

        boolean usingClasses = (json.has("classes") && ((json.get("classes").isJsonPrimitive() && json.get("classes").getAsJsonPrimitive().isBoolean() && json.get("classes").getAsBoolean()) || json.get("classes").isJsonArray()));

        return new MapInfo(name, version, authors, gameType, parsedTeams, usingClasses, json);
    }

}
