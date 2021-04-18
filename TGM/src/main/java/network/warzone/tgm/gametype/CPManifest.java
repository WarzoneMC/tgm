package network.warzone.tgm.gametype;

import network.warzone.tgm.match.MatchManifest;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.koth.KOTHModule;

import java.util.ArrayList;
import java.util.List;

public class CPManifest extends MatchManifest {
    @Override
    public List<MatchModule> allocateGameModules() {
        List<MatchModule> matchModules = new ArrayList<>();
        matchModules.add(new KOTHModule());
        return matchModules;
    }
}
