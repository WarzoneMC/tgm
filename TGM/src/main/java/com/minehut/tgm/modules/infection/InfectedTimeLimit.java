package com.minehut.tgm.modules.infection;

import com.minehut.tgm.TGM;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.match.MatchStatus;
import com.minehut.tgm.modules.team.TeamManagerModule;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by Draem on 8/5/2017.
 */
public class InfectedTimeLimit extends MatchModule {

    private Match match;
    private TeamManagerModule teamManager;

    @Override
    public void load(Match match) {
        this.match = match;
        this.teamManager = match.getModule(TeamManagerModule.class);
    }

    public void startCountdown(int length) {

        new BukkitRunnable() {
            @Override
            public void run() {
                if (match.getMatchStatus().equals(MatchStatus.MID) && teamManager.getTeamById("humans").getMembers().size() != 0) {
                    TGM.get().getMatchManager().endMatch(teamManager.getTeamById("humans"));
                }
            }
        }.runTaskLater(TGM.get(), length * 60 * 20);
    }

}
