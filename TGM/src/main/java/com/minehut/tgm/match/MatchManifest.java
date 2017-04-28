package com.minehut.tgm.match;

import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
public abstract class MatchManifest {

    /**
     * Determines which modules to load based on the
     * given gametype.
     */
    public abstract List<MatchModule> selectModules();
}
