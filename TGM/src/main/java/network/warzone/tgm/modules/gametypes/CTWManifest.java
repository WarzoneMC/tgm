package network.warzone.tgm.modules.gametypes;

import network.warzone.tgm.match.MatchManifest;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.gametypes.ctw.CTWModule;
import network.warzone.tgm.modules.wool.WoolChestModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
public class CTWManifest extends MatchManifest {

    @Override
    public List<MatchModule> allocateGameModules() {
        List<MatchModule> matchModules = new ArrayList<>();
        matchModules.add(new WoolChestModule());
        matchModules.add(new CTWModule());
        return matchModules;
    }
}
