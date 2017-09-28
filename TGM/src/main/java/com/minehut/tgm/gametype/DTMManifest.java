package com.minehut.tgm.gametype;

import com.minehut.tgm.match.MatchManifest;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.dtm.DTMModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
public class DTMManifest extends MatchManifest {

    @Override
    public List<MatchModule> allocateGameModules() {
        List<MatchModule> matchModules = new ArrayList<>();
        matchModules.add(new DTMModule());
        return matchModules;
    }
}
