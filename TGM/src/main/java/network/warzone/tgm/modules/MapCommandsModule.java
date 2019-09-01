package network.warzone.tgm.modules;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.MatchModule;

public class MapCommandsModule extends MatchModule {
    @Override
    public void enable() {
        TGM.get().getServer().broadcastMessage("test, the match started");
    }
}
