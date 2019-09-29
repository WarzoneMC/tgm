package network.warzone.tgm.gametype;

import network.warzone.tgm.match.MatchManifest;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.blitz.BlitzModule;
import network.warzone.tgm.modules.death.DeathMessageModule;
import network.warzone.tgm.modules.respawn.RespawnModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorge on 10/7/2017.
 */
public class BlitzManifest extends MatchManifest {

    @Override
    public List<MatchModule> allocateGameModules() {
        List<MatchModule> matchModules = new ArrayList<>();
        matchModules.add(new BlitzModule());
        return matchModules;
    }

    @Override
    public List<MatchModule> allocateCoreModules() {
        List<MatchModule> modules = super.allocateCoreModules();
        List<MatchModule> toRemove = new ArrayList<>();
        for (MatchModule module : modules) {
            if (module instanceof RespawnModule) toRemove.add(module);
        }
        modules.removeAll(toRemove);
        return modules;
    }
}
