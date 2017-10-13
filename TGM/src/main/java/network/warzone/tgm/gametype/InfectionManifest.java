package network.warzone.tgm.gametype;

import network.warzone.tgm.match.MatchManifest;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.DeathMessageModule;
import network.warzone.tgm.modules.gametypes.infection.InfectedTimeLimit;
import network.warzone.tgm.modules.gametypes.infection.InfectionModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Draem on 7/31/2017.
 */
public class InfectionManifest extends MatchManifest {

    @Override
    public List<MatchModule> allocateGameModules() {
        List<MatchModule> matchModules = new ArrayList<>();

        matchModules.add(new InfectionModule());
        matchModules.add(new InfectedTimeLimit());

        allocateCoreModules().remove(new DeathMessageModule()); // Replaced by InfectionModule

        return matchModules;
    }

}
