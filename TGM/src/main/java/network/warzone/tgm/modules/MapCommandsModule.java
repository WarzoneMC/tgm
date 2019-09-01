package network.warzone.tgm.modules;

import com.google.gson.JsonElement;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;

public class MapCommandsModule extends MatchModule {
    Match match;

    String startCommand;

    @Override
    public void enable() {
        if (this.startCommand != null)
            TGM.get().getServer().dispatchCommand(TGM.get().getServer().getConsoleSender(), this.startCommand);
    }

    @Override
    public void load(Match match) {
        this.match = match;

        if (match.getMapContainer().getMapInfo().getJsonObject().has("startCommand"))
            this.startCommand = match.getMapContainer().getMapInfo().getJsonObject().get("startCommand").getAsString();
    }
}
