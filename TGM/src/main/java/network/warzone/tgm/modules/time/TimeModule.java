package network.warzone.tgm.modules.time;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.*;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

@ModuleData(load = ModuleLoadTime.EARLIEST) @Getter
public class TimeModule extends MatchModule {
    private long startedTimeStamp = 0;
    private long endedTimeStamp = 0;

    private List<Broadcast> broadcasts = new ArrayList<>();
    private List<TimeSubscriber> timeSubscribers = new ArrayList<>();

    @Setter private boolean timeLimited = false;
    @Setter private int timeLimit = 20*60; // Default (20 minutes)
    private MatchTeam defaultWinner = null;
    //@Getter private List<TimeLimitService> services = new ArrayList<>();
    @Setter private TimeLimitService timeLimitService;

    private int taskID;

    @Override
    public void load(Match match) {
        if (match.getMapContainer().getMapInfo().getJsonObject().has("time")) {
            JsonObject timeObject = match.getMapContainer().getMapInfo().getJsonObject().get("time").getAsJsonObject();
            if (timeObject.has("limit")) {
                timeLimit = timeObject.get("limit").getAsInt();
                setTimeLimited(true);
            }
            if (timeObject.has("defaultWinner")) defaultWinner = match.getModule(TeamManagerModule.class).getTeamById(timeObject.get("defaultWinner").getAsString());
            if (timeObject.has("broadcasts") && timeObject.get("broadcasts").isJsonArray()) {
                for (JsonElement element : timeObject.getAsJsonArray("broadcasts")) {
                    if (!element.isJsonObject()) continue;
                    JsonObject broadcast = (JsonObject) element;
                    boolean repeat = broadcast.get("repeat").getAsBoolean();
                    String message = broadcast.has("message") ? broadcast.get("message").getAsString() : null;
                    List<String> commands = new ArrayList<>();
                    for (JsonElement cmdElement : broadcast.get("commands").getAsJsonArray()) {
                        String command = cmdElement.getAsString();
                        commands.add(command);
                    }
                    int interval = broadcast.get("interval").getAsInt();
                    List<Integer> exclude = new ArrayList<>();
                    if (broadcast.has("exclude") && broadcast.get("exclude").isJsonArray()) broadcast.get("exclude").getAsJsonArray().forEach(jsonElement -> exclude.add(jsonElement.getAsInt()));
                    broadcasts.add(new Broadcast(message, commands, interval, repeat, exclude));
                }
            }
        }
    }

    @Override
    public void enable() {
        startedTimeStamp = System.currentTimeMillis();
        taskID = Bukkit.getScheduler().runTaskTimer(TGM.get(), () -> {
            int time = (int) getTimeElapsed();
            for (TimeSubscriber module : timeSubscribers) {
                module.processSecond(time);
            }
            for (Broadcast broadcast : broadcasts) {
                broadcast.run(time);
            }
            if (isTimeLimited() && time >= timeLimit) {
                endMatch();
            }
        }, 20, 20).getTaskId();
    }

    public void endMatch() {
        MatchTeam winnerTeam = defaultWinner;
        if (getTimeLimitService() != null) {
            winnerTeam = getTimeLimitService().getWinnerTeam();
        }
        TGM.get().getMatchManager().endMatch(winnerTeam);
    }

    @Override
    public void disable() {
        endedTimeStamp = System.currentTimeMillis();
        setTimeLimited(false);
        Bukkit.getScheduler().cancelTask(taskID);

        broadcasts.clear();
    }

    /**
     * @return Time elapsed in seconds
     */
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
