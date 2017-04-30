package com.minehut.tgm.modules;

import com.minehut.tgm.TGM;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.match.MatchStatus;
import lombok.Getter;

public class TimeModule extends MatchModule {
    @Getter private long startedTimeStamp = 0;
    @Getter private long endedTimeStamp = 0;

    @Override
    public void enable() {
        startedTimeStamp = System.currentTimeMillis();
    }

    public double getTimeElapsed() {
        MatchStatus matchStatus = TGM.getMatchManager().getMatch().getMatchStatus();
        if (matchStatus == MatchStatus.MID) {
            return (double) ((System.currentTimeMillis() - startedTimeStamp) / 1000);
        } else {
            return 0;
        }
    }

}
