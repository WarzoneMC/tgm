package com.minehut.tgm.modules;

import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class GameRuleModule extends MatchModule {

    static {
        setGameRules(Bukkit.getWorlds().get(0)); //Set gamerules in main unused world
    }

    @Override
    public void load(Match match) {
        setGameRules(match.getWorld());
    }

    private static void setGameRules(World world) {
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("commandBlockOutput", "false");
        world.setGameRuleValue("logAdminCommands", "false");
        world.setGameRuleValue("doWeatherCycle", "false");
        world.setGameRuleValue("disableElytraMovementCheck", "true");
        world.setGameRuleValue("announceAdvancements", "false");
    }
}
