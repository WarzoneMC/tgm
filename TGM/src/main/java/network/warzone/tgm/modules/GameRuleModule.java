package network.warzone.tgm.modules;

import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import org.bukkit.GameRule;
import org.bukkit.World;

public class GameRuleModule extends MatchModule {

    @Override
    public void load(Match match) {
        setGameRules(match.getWorld());
    }

    public static void setGameRules(World world) {
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, true);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
    }
}
