package network.warzone.tgm.clickevent;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.parser.item.ItemDeserializer;
import network.warzone.tgm.util.Parser;
import network.warzone.tgm.util.Strings;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorge on 10/11/2019
 */
public abstract class ClickEvent {

    public abstract void run(Match match, Player player);

    public enum Action {
        OPEN_SCREEN,
        CLOSE_SCREEN,
        TRADE,
        TELEPORT,
        RUN_AS_PLAYER,
        RUN_AS_CONSOLE;

        public static Action fromInput(String input) {
            for (Action action : values()) {
                if (Strings.getTechnicalName(input).equalsIgnoreCase(action.name())) return action;
            }
            return null;
        }
    }

    public static ClickEvent deserialize(JsonObject jsonObject) {
        Action action = Action.fromInput(jsonObject.get("action").getAsString());
        if (action == null) {
            return null;
        }
        JsonElement value = jsonObject.get("value");
        switch (action) {
            case OPEN_SCREEN:
                return new OpenScreenClickEvent(value.getAsString());
            case CLOSE_SCREEN:
                return new CloseScreenClickEvent();
            case TRADE:
                List<ItemStack> give = new ArrayList<>(), take = new ArrayList<>();
                if (!value.isJsonObject()) return null;
                JsonObject valueObject = value.getAsJsonObject();
                for (JsonElement jsonElement : valueObject.getAsJsonArray("give"))
                    give.add(ItemDeserializer.parse(jsonElement));
                for (JsonElement jsonElement : valueObject.getAsJsonArray("take"))
                    take.add(ItemDeserializer.parse(jsonElement));
                return new TradeClickEvent(give, take);
            case TELEPORT:
                return new TeleportClickEvent(Parser.convertLocation(null, value));
            case RUN_AS_PLAYER:
                List<String> playerCommands = new ArrayList<>();
                for (JsonElement element : value.getAsJsonArray()) {
                    playerCommands.add(element.getAsString());
                }
                return new RunAsPlayerClickEvent(playerCommands);
            case RUN_AS_CONSOLE:
                List<String> consoleCommands = new ArrayList<>();
                for (JsonElement element : value.getAsJsonArray()) {
                    consoleCommands.add(element.getAsString());
                }
                return new RunAsConsoleClickEvent(consoleCommands);
            default:
                return null;
        }
    }

}
