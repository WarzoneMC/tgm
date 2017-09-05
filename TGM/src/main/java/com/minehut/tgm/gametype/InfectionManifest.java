package com.minehut.tgm.gametype;

import com.minehut.tgm.match.MatchManifest;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.DeathMessageModule;
import com.minehut.tgm.modules.infection.InfectedTimeLimit;
import com.minehut.tgm.modules.infection.InfectionModule;

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
