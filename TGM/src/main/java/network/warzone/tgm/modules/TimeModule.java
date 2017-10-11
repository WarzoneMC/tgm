package network.warzone.tgm.modules;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.match.ModuleData;
import network.warzone.tgm.match.ModuleLoadTime;
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
