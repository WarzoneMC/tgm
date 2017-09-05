package com.minehut.tgm.gametype;

import com.minehut.tgm.match.MatchManifest;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.tdm.TDMModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
public class TDMManifest extends MatchManifest {

    @Override
    public List<MatchModule> allocateGameModules() {
        List<MatchModule> matchModules = new ArrayList<>();
        matchModules.add(new TDMModule());
        return matchModules;
    }
}
