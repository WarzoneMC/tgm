package network.warzone.tgm.modules.time;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Setter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.*;
import lombok.Getter;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

@ModuleData(load = ModuleLoadTime.EARLIEST)
public class TimeModule extends MatchModule {
    @Getter private long startedTimeStamp = 0;
    @Getter private long endedTimeStamp = 0;

    private List<Broadcast> broadcasts = new ArrayList<>();

    @Getter @Setter private boolean timeLimited = false;
    @Getter @Setter private int timeLimit = 20*60; // Default

    private int taskID;

    @Override
    public void load(Match match) {
        if (match.getMapContainer().getMapInfo().getJsonObject().has("time")) {
            JsonObject timeObject = match.getMapContainer().getMapInfo().getJsonObject().get("time").getAsJsonObject();
            if (timeObject.has("limit")) {
                timeLimit = timeObject.get("limit").getAsInt();
                setTimeLimited(true);
            }
            if (timeObject.has("broadcasts") && timeObject.get("broadcasts").isJsonArray()) {
                for (JsonElement element : timeObject.getAsJsonArray("broadcasts")) {
                    JsonObject broadcast = (JsonObject) element;
                    BroadcastTimeType type = BroadcastTimeType.valueOf(broadcast.get("type").getAsString().toUpperCase());
                    String message = broadcast.get("message").getAsString();
                    int interval = broadcast.get("value").getAsInt();
                    List<Integer> excludedTimes = new ArrayList<>();
                    if (broadcast.has("exclude") && broadcast.get("exclude").isJsonArray()) broadcast.get("exclude").getAsJsonArray().forEach(jsonElement -> excludedTimes.add(jsonElement.getAsInt()));
                    broadcasts.add(new Broadcast(type, message, interval, excludedTimes));
                }
            }
        }
    }

    @Override
    public void enable() {
        startedTimeStamp = System.currentTimeMillis();
        taskID = Bukkit.getScheduler().runTaskTimer(TGM.get(), () -> {
            int time = (int) getTimeElapsed();
            for (Broadcast broadcast : broadcasts) {
                broadcast.run(time);
            }
            if (isTimeLimited()) {
                if (time >= timeLimit) {
                    TGM.get().getMatchManager().endMatch(TGM.get().getModule(TeamManagerModule.class).getTeams().get(0));
                }
            }
        }, 20, 20).getTaskId();
    }

    @Override
    public void disable() {
        endedTimeStamp = System.currentTimeMillis();
        setTimeLimited(false);
        Bukkit.getScheduler().cancelTask(taskID);
    }

    public double getTimeElapsed() {
        MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();
        if (matchStatus == MatchStatus.MID) {
            return (double) ((System.currentTimeMillis() - startedTimeStamp) / 1000);
        } else if (matchStatus == MatchStatus.POST) {
            return (double) ((endedTimeStamp - startedTimeStamp) / 1000);
        } else {
            return 0;
        }
    }

}
