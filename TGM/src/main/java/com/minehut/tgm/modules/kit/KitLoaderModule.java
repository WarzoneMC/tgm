package com.minehut.tgm.modules.kit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.kit.parser.ArmorKitNodeParser;
import com.minehut.tgm.modules.kit.parser.ItemKitNodeParser;
import com.minehut.tgm.modules.kit.parser.KitNodeParser;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


public class KitLoaderModule extends MatchModule {
    @Getter
    private final List<KitNodeParser> parsers = new ArrayList<>();

    public KitLoaderModule() {
        parsers.add(new ItemKitNodeParser());
        parsers.add(new ArmorKitNodeParser());
    }

    @Override
    public void load(Match match) {
        if (match.getMapContainer().getMapInfo().getJsonObject().has("kits")) {
            for (JsonElement kitElement : match.getMapContainer().getMapInfo().getJsonObject().getAsJsonArray("kits")) {
                JsonObject kitJson = kitElement.getAsJsonObject();

                String name = "";
                if (kitJson.has("name")) {
                    name = kitJson.get("name").getAsString();
                }

                String description = "";
                if (kitJson.has("description")) {
                    description = kitJson.get("description").getAsString();
                }

                List<MatchTeam> teams = new ArrayList<>();
                TeamManagerModule teamManagerModule = match.getModule(TeamManagerModule.class);
                if (kitJson.has("teams")) {
                    for (Object o : kitJson.getAsJsonArray("teams")) {
                        MatchTeam matchTeam = teamManagerModule.getTeamById(((JsonPrimitive) o).getAsString());
                        if (matchTeam != null) {
                            teams.add(matchTeam);
                        }
                    }
                } else {
                    //if teams aren't specified, give it all by default.
                    for (MatchTeam matchTeam : teamManagerModule.getTeams()) {
                        if (!matchTeam.isSpectator()) {
                            teams.add(matchTeam);
                        }
                    }
                }

                List<KitNode> nodes = new ArrayList<>();
                for (JsonElement nodeElement : kitJson.getAsJsonArray("items")) {
                    JsonObject nodeJson = nodeElement.getAsJsonObject();

                    for (KitNodeParser parser : parsers) {
                        try {
                            List<KitNode> result = parser.parse(nodeJson);
                            if (result != null) {
                                nodes.addAll(result);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                Kit kit = new Kit(name, description, nodes);

                for (MatchTeam matchTeam : teams) {
                    matchTeam.addKit(kit);
                }

            }
        }
    }
}
