package com.minehut.tgm.modules;

import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import org.bukkit.World;

public class GameRuleModule extends MatchModule {

    @Override
    public void load(Match match) {
        setGameRules(match.getWorld());
    }

    public static void setGameRules(World world) {
        world.setGameRuleValue("keepInventory", "true");
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("commandBlockOutput", "false");
        world.setGameRuleValue("logAdminCommands", "false");
        world.setGameRuleValue("doWeatherCycle", "false");
        world.setGameRuleValue("disableElytraMovementCheck", "true");
        world.setGameRuleValue("announceAdvancements", "false");
    }
}
