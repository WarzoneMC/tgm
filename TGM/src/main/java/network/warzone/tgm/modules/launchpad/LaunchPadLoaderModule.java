package network.warzone.tgm.modules.launchpad;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.ModuleData;
import network.warzone.tgm.match.ModuleLoadTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorge on 10/08/2019
 */
@ModuleData(load = ModuleLoadTime.LATEST) @Getter
public class LaunchPadLoaderModule extends MatchModule {

    private List<LaunchPadModule> launchPads = new ArrayList<>();

    @Override
    public void load(Match match) {
        JsonObject jsonObject = match.getMapContainer().getMapInfo().getJsonObject();
        if (jsonObject.has("launchpads") && jsonObject.get("launchpads").isJsonArray()) {
            for (JsonElement element : jsonObject.getAsJsonArray("launchpads")) {
                if (!element.isJsonObject()) continue;
                try {
                    LaunchPadModule launchPad = LaunchPadModule.deserialize(element.getAsJsonObject());
                    match.getModules().add(launchPad);
                    launchPad.load(match);
                    this.launchPads.add(launchPad);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
