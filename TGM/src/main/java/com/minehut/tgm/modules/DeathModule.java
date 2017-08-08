package com.minehut.tgm.modules;

import com.minehut.tgm.TGM;
import com.minehut.tgm.match.MatchModule;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathModule extends MatchModule implements Listener {

    /**
     * Kills a player when they join a team
     * after being previously on an actual team.
     *
     * This is to prevent players from "combat logging"
     * by team swapping to avoid getting a death.
     */

    // not need because i prevent people changing teams during the game
//    @EventHandler
//    public void onTeamChange(TeamChangeEvent event) {
//        if (TGM.get().getMatchManager().getMatch().getMatchStatus() == MatchStatus.MID) {
//            if (event.getOldTeam() != null && !event.getOldTeam().isSpectator()) {
//                event.getPlayerContext().getPlayer().setHealth(0);
//            }
//        }
//    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Bukkit.getScheduler().runTaskLater(TGM.get(), new Runnable() {
            @Override
            public void run() {
                event.getEntity().spigot().respawn();
            }
        }, 0L);
    }
}
