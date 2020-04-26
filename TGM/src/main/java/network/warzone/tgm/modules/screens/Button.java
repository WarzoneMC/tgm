package network.warzone.tgm.modules.screens;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.clickevent.ClickEvent;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.parser.item.ItemDeserializer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorge on 10/11/2019
 */
@Getter
public class Button {

    private Match match;
    private ItemStack item;
    private int slot;
    private List<MatchTeam> teams;
    private List<ClickEvent> clickEvents;

    public Button(Match match, ItemStack item, int slot, List<MatchTeam> teams, List<ClickEvent> clickEvents) {
        this.match = match;
        this.item = item;
        this.slot = slot;
        this.teams = teams;
        this.clickEvents = clickEvents;
    }

    public static Button deserialize(JsonObject jsonObject) {
        ItemStack item = ItemDeserializer.parse(jsonObject.get("item"));
        int slot = jsonObject.get("slot").getAsInt();
        List<MatchTeam> teams = new ArrayList<>();
        List<ClickEvent> events = new ArrayList<>();
        if (jsonObject.has("teams"))
            teams.addAll(TGM.get().getModule(TeamManagerModule.class).getTeams(jsonObject.getAsJsonArray("teams")));
        if (jsonObject.has("clickEvents")) {
            if (jsonObject.get("clickEvents").isJsonArray()) {
                for (JsonElement jsonElement : jsonObject.getAsJsonArray("clickEvents")) {
                    if (!jsonElement.isJsonObject()) continue;
                    events.add(ClickEvent.deserialize(jsonElement.getAsJsonObject()));
                }
            } else if (jsonObject.get("clickEvents").isJsonObject()) {
                events.add(ClickEvent.deserialize(jsonObject.getAsJsonObject("clickEvents")));
            }
        }
        return new Button(TGM.get().getMatchManager().getMatch(), item, slot, teams, events);
    }

}
