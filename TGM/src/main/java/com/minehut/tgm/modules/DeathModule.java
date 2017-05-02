package com.minehut.tgm.modules;

import com.minehut.tgm.TGM;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.match.MatchStatus;
import com.minehut.tgm.modules.team.TeamChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DeathModule extends MatchModule implements Listener {

    /**
     * Kills a player when they join a team
     * after being previously on an actual team.
     *
     * This is to prevent players from "combat logging"
     * by team swapping to avoid getting a death.
     */
    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (TGM.get().getMatchManager().getMatch().getMatchStatus() == MatchStatus.MID) {
            if (event.getOldTeam() != null && !event.getOldTeam().isSpectator()) {
                event.getPlayerContext().getPlayer().setHealth(0);
            }
        }
    }
}
