package com.minehut.tgm.modules;

import com.minehut.tgm.TGM;
import com.minehut.tgm.join.MatchJoinEvent;
import com.minehut.tgm.map.SpawnPoint;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.match.MatchStatus;
import com.minehut.tgm.team.MatchTeam;
import com.minehut.tgm.team.TeamChangeEvent;
import com.minehut.tgm.user.PlayerContext;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SpawnPointHandlerModule extends MatchModule implements Listener {

    @EventHandler
    public void onJoin(MatchJoinEvent event) {
        event.getPlayerContext().getPlayer().teleport(getTeamSpawn(TGM.getTgm().getTeamManager().getSpectators()).getLocation());
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (TGM.getMatchManager().getMatch().getMatchStatus() == MatchStatus.MID) {
            event.getPlayerContext().getPlayer().teleport(getTeamSpawn(event.getTeam()).getLocation());
        }
    }

    @Override
    public void enable() {
        for (MatchTeam matchTeam : TGM.getTgm().getTeamManager().getTeams()) {
            if (!matchTeam.isSpectator()) {
                for (PlayerContext player : matchTeam.getMembers()) {
                    player.getPlayer().teleport(getTeamSpawn(matchTeam).getLocation());
                }
            }
        }
    }

    private SpawnPoint getTeamSpawn(MatchTeam matchTeam) {
        //todo: actually randomize spawn points instead of grabbing first one.
        return matchTeam.getSpawnPoints().get(0);
    }
}
