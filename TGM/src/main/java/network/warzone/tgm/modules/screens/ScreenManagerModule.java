package network.warzone.tgm.modules.screens;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jorge on 10/10/2019
 */
public class ScreenManagerModule extends MatchModule {

    private Match match;

    private Map<String, Screen> screens = new HashMap<>();

    public Screen getScreen(String id) {
        return screens.get(id);
    }

    @Override
    public void load(Match match) {
        this.match = match;
        JsonObject jsonObject = match.getMapContainer().getMapInfo().getJsonObject();
        if (jsonObject.has("screens")) {
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("screens")) {
                if (!jsonElement.isJsonObject()) continue;
                getScreen(jsonElement);
            }
        }
    }

    @Override
    public void disable() {
        screens.forEach((id, screen) -> screen.disable());
    }

    public Screen getScreen(JsonElement jsonElement) {
        Preconditions.checkArgument(jsonElement.isJsonPrimitive() || jsonElement.isJsonObject(), "Screen must a JSON object or a screen ID.");
        if (jsonElement.isJsonPrimitive()) {
            return getScreen(jsonElement.getAsString());
        } else if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String title = jsonObject.get("title").getAsString();
            int size = jsonObject.get("size").getAsInt();
            List<Button> buttons = new ArrayList<>();
            if (jsonObject.has("buttons") && jsonObject.get("buttons").isJsonArray()) {
                for (JsonElement buttonElement : jsonObject.getAsJsonArray("buttons")) {
                    if (!buttonElement.isJsonObject()) continue;
                    buttons.add(Button.deserialize(buttonElement.getAsJsonObject()));
                }
            }
            Screen screen = new Screen(this.match, title, size, buttons);
            if (jsonObject.has("id")) {
                this.screens.put(jsonObject.get("id").getAsString(), screen);
            }
            return screen;
        }
        return null;
    }

}
