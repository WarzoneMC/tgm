package com.minehut.tgm.gametype;

import com.minehut.tgm.match.MatchManifest;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.koth.KOTHModule;
import com.minehut.tgm.modules.wool.WoolChestModule;

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
        return matchModules;
    }
}
