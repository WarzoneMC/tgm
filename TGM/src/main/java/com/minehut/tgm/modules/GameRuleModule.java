package com.minehut.tgm.modules;

import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;

public class GameRuleModule extends MatchModule {

    @Override
    public void load(Match match) {
        match.getWorld().setGameRuleValue("doMobSpawning", "false");
        match.getWorld().setGameRuleValue("doDaylightCycle", "false");
    }
}
