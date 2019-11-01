package network.warzone.tgm.gametype;

import network.warzone.tgm.match.MatchManifest;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.gametypes.infection.InfectionModule;

import java.util.Collections;
import java.util.List;

/**
 * Created by Draem on 7/31/2017.
 */
public class InfectionManifest extends MatchManifest {

    @Override
    public List<MatchModule> allocateGameModules() {
        return Collections.singletonList(new InfectionModule());
    }

}
