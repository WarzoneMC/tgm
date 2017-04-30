package com.minehut.tgm.modules;

import com.minehut.tgm.TGM;
import com.minehut.tgm.map.SpawnPoint;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.match.MatchStatus;
import com.minehut.tgm.team.MatchTeam;
import com.minehut.tgm.team.TeamChangeEvent;
import com.minehut.tgm.user.PlayerContext;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SpawnPointHandlerModule extends MatchModule implements Listener {

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (TGM.getMatchManager().getMatch().getMatchStatus() == MatchStatus.MID) {
            SpawnPoint spawnPoint = event.getTeam().getSpawnPoints().get(0);
            event.getPlayerContext().getPlayer().teleport(spawnPoint.getLocation());
        }
    }

    @Override
    public void enable() {
        for (MatchTeam matchTeam : TGM.getTgm().getTeamManager().getTeams()) {
            if (!matchTeam.isSpectator()) {
                for (PlayerContext player : matchTeam.getMembers()) {
                    SpawnPoint spawnPoint = matchTeam.getSpawnPoints().get(0);
                    player.getPlayer().teleport(spawnPoint.getLocation());
                }
            }
        }
    }
}
