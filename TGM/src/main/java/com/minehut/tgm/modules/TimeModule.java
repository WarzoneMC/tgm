package com.minehut.tgm.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minehut.tgm.TGM;
import com.minehut.tgm.match.*;
import com.minehut.tgm.modules.team.TeamManagerModule;
import lombok.Getter;
import org.bukkit.Bukkit;

@ModuleData(load = ModuleLoadTime.EARLIEST)
public class TimeModule extends MatchModule {
    @Getter private long startedTimeStamp = 0;
    @Getter private long endedTimeStamp = 0;

    @Getter protected int runnableId;
    @Getter private int timeLimit = 0;

    @Override
    public void enable() {
        startedTimeStamp = System.currentTimeMillis();
    }

    @Override
    public void load(Match match){
        if (match.getMapContainer().getMapInfo().getJsonObject().has("time")) {
            JsonObject timeJson = match.getMapContainer().getMapInfo().getJsonObject().get("time").getAsJsonObject();
            timeLimit = timeJson.get("limit").getAsInt();
        }

        if (timeLimit > 0) {
            runnableId = Bukkit.getScheduler().scheduleSyncRepeatingTask(TGM.get(), new Runnable() {
                @Override
                public void run() {
                    if (timeLimit > getTimeElapsed()) return;
                    TGM.get().getMatchManager().endMatch(TGM.get().getModule(TeamManagerModule.class).getTeams().get(1));
                    Bukkit.getScheduler().cancelTask(runnableId);
                }
            }, 20L, 20L);
        }
    }

    @Override
    public void unload(){
        if (timeLimit > 0) {
            Bukkit.getScheduler().cancelTask(runnableId);
        }
    }

    @Override
    public void disable() {
        endedTimeStamp = System.currentTimeMillis();
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
