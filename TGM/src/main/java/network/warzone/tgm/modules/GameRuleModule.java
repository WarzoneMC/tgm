package network.warzone.tgm.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import org.bukkit.GameRule;
import org.bukkit.World;

import java.util.Map;

public class GameRuleModule extends MatchModule {

    @Override
    public void load(Match match) {
        setGameRuleDefaults(match.getWorld());
        JsonObject jsonObject = match.getMapContainer().getMapInfo().getJsonObject();
        if (jsonObject.has("gamerules")) {
            for (Map.Entry<String, JsonElement> gameRuleEntry : jsonObject.getAsJsonObject("gamerules").entrySet()) {
                if (!gameRuleEntry.getValue().isJsonPrimitive()) continue;
                GameRule gameRule = GameRule.getByName(gameRuleEntry.getKey());
                if (gameRule == null) continue;
                if (gameRule.getType() == Boolean.class) {
                    match.getWorld().setGameRule(gameRule, gameRuleEntry.getValue().getAsBoolean());
                } else if (gameRule.getType() == Integer.class) {
                    match.getWorld().setGameRule(gameRule, gameRuleEntry.getValue().getAsInt());
                }
            }
        }
    }

    public static void setGameRuleDefaults(World world) {
        world.setGameRule(GameRule.KEEP_INVENTORY, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, true);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
    }
}
