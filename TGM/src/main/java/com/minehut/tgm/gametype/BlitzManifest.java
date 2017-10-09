package com.minehut.tgm.gametype;

import com.minehut.tgm.match.MatchManifest;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.blitz.BlitzModule;
import com.minehut.tgm.modules.tdm.TDMModule;

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
}
