package network.warzone.tgm.modules;

import com.google.gson.JsonElement;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;

import java.util.ArrayList;
import java.util.List;

// TODO: Make a better event actions module
public class MapCommandsModule extends MatchModule {

    private Match match;
    private List<String> startCommands = new ArrayList<>();

    @Override
    public void enable() {
        if (this.startCommands != null)
            startCommands.forEach(c -> TGM.get().getServer().dispatchCommand(TGM.get().getServer().getConsoleSender(), c));
    }

    @Override
    public void load(Match match) {
        if (match.getMapContainer().getMapInfo().getJsonObject().has("startCommands")) {
            for (JsonElement jsonElement : match.getMapContainer().getMapInfo().getJsonObject().getAsJsonArray("startCommands")) {
                if (jsonElement.isJsonPrimitive()) continue;
                this.startCommands.add(jsonElement.getAsString());
            }
        }
    }
}
