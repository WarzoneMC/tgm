package network.warzone.tgm.modules;

import com.google.gson.JsonElement;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;

public class MapCommandsModule extends MatchModule {
    Match match;

    @Override
    public void enable() {
        TGM.get().getServer().broadcastMessage("test, the match started");
        //match.getMapContainer().getMapInfo().getJsonObject()
    }

    @Override
    public void load(Match match) {
        this.match = match;
        if (match.getMapContainer().getMapInfo().getJsonObject().has("startCommand")) {
            JsonElement command = match.getMapContainer().getMapInfo().getJsonObject().get("startCommand");
            TGM.get().getServer().broadcastMessage(command.toString());
        }
    }
}
