package com.minehut.tgm.modules;

import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;

public class GameRuleModule extends MatchModule {

    @Override
    public void load(Match match) {
        match.getWorld().setGameRuleValue("doMobSpawning", "false");
        match.getWorld().setGameRuleValue("doDaylightCycle", "false");
        match.getWorld().setGameRuleValue("commandBlockOutput", "false");
        match.getWorld().setGameRuleValue("logAdminCommands", "false");
        match.getWorld().setGameRuleValue("doWeatherCycle", "false");
        match.getWorld().setGameRuleValue("disableElytraMovementCheck", "true");
        match.getWorld().setGameRuleValue("announceAdvancements", "false");
    }
}
