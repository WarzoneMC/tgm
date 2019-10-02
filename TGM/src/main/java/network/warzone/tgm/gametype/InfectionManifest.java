package network.warzone.tgm.gametype;

import network.warzone.tgm.match.MatchManifest;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.death.DeathMessageModule;
import network.warzone.tgm.modules.infection.InfectionModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Draem on 7/31/2017.
 */
public class InfectionManifest extends MatchManifest {

    @Override
    public List<MatchModule> allocateGameModules() {
        return new ArrayList<MatchModule>(){
            {
                add(new InfectionModule());
            }
        };
    }

    @Override
    public List<MatchModule> allocateCoreModules() {
        List<MatchModule> modules = super.allocateCoreModules();
        List<MatchModule> toRemove = new ArrayList<>();
        for (MatchModule module : modules) {
            if (module instanceof DeathMessageModule) toRemove.add(module);
        }
        modules.removeAll(toRemove);
        return modules;
    }

}
