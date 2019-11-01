package network.warzone.tgm.modules.countdown;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.ModuleData;
import network.warzone.tgm.match.ModuleLoadTime;
import network.warzone.tgm.modules.tasked.TaskedModuleManager;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.util.Parser;
import network.warzone.tgm.util.Strings;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import java.util.*;

/**
 * Created by Jorge on 10/20/2019
 */
@ModuleData(load = ModuleLoadTime.EARLIEST)
public class CountdownManagerModule extends MatchModule {

    private Match match;
    private TeamManagerModule teamManagerModule;
    private TaskedModuleManager taskedModuleManager;

    private Map<String, CustomCountdown> customCountdowns = new HashMap<>();

    @Override
    public void load(Match match) {
        this.match = match;
        this.teamManagerModule = match.getModule(TeamManagerModule.class);
        this.taskedModuleManager = match.getModule(TaskedModuleManager.class);
        match.getModules().add(new StartCountdown());
        match.getModules().add(new CycleCountdown());
        JsonObject jsonObject = match.getMapContainer().getMapInfo().getJsonObject();
        if (jsonObject.has("countdowns")) {
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("countdowns")) {
                try {
                    getCountdown(jsonElement);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        customCountdowns.values().forEach(countdown -> match.getModules().add(countdown));
    }

    public CustomCountdown getCountdown(JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            return this.customCountdowns.get(jsonElement.getAsString());
        } else {
            JsonObject countdownObj = jsonElement.getAsJsonObject();
            int time = countdownObj.get("time").getAsInt(); // seconds
            String title = countdownObj.get("title").getAsString();
            BarColor color = BarColor.PURPLE;
            BarStyle style = BarStyle.SOLID;
            boolean visible = !countdownObj.has("visible") || countdownObj.get("visible").getAsBoolean();
            boolean invert = countdownObj.has("invert") && countdownObj.get("invert").getAsBoolean();
            List<MatchTeam> teams = new ArrayList<>();
            List<String> onFinish = new ArrayList<>();
            if (countdownObj.has("color")) color = BarColor.valueOf(Strings.getTechnicalName(countdownObj.get("color").getAsString()));
            if (countdownObj.has("style")) style = BarStyle.valueOf(Strings.getTechnicalName(countdownObj.get("style").getAsString()));
            if (countdownObj.has("teams")) teams.addAll(Parser.getTeamsFromElement(teamManagerModule, countdownObj.get("teams")));
            if (countdownObj.has("onFinish")) {
                for (JsonElement cmdElement : countdownObj.getAsJsonArray("onFinish")) {
                    if (!cmdElement.isJsonPrimitive()) continue;
                    onFinish.add(cmdElement.getAsString());
                }
            }
            CustomCountdown customCountdown = new CustomCountdown(time, title, color, style, visible, invert, teams, onFinish);
            if (countdownObj.has("id")) {
                this.customCountdowns.put(countdownObj.get("id").getAsString(), customCountdown);
            }
            return customCountdown;
        }
    }

    public CustomCountdown getCountdown(String id) {
        return this.customCountdowns.get(id);
    }

    public void addCountdown(String id, CustomCountdown countdown) {
        this.customCountdowns.put(id, countdown);
        TGM.registerEvents(countdown);
        this.match.getModules().add(countdown);
        this.taskedModuleManager.addTaskedModule(countdown);
    }

    public Map<String, CustomCountdown> getCustomCountdowns() {
        return new HashMap<>(customCountdowns);
    }
}
