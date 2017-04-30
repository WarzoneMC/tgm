package com.minehut.tgm.modules;

import com.minehut.tgm.TGM;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.match.MatchStatus;
import com.minehut.tgm.match.ModuleData;
import com.minehut.tgm.match.ModuleLoadTime;
import lombok.Getter;

@ModuleData(load = ModuleLoadTime.EARLIEST)
public class TimeModule extends MatchModule {
    @Getter private long startedTimeStamp = 0;
    @Getter private long endedTimeStamp = 0;

    @Override
    public void enable() {
        startedTimeStamp = System.currentTimeMillis();
    }

    @Override
    public void disable() {
        endedTimeStamp = System.currentTimeMillis();
    }

    public double getTimeElapsed() {
        MatchStatus matchStatus = TGM.getMatchManager().getMatch().getMatchStatus();
        if (matchStatus == MatchStatus.MID) {
            return (double) ((System.currentTimeMillis() - startedTimeStamp) / 1000);
        } else if (matchStatus == MatchStatus.POST) {
            return (double) ((endedTimeStamp - startedTimeStamp) / 1000);
        } else {
            return 0;
        }
    }

}
