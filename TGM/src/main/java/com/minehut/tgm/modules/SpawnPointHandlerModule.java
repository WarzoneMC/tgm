package com.minehut.tgm.modules;

import com.minehut.tgm.map.SpawnPoint;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.team.TeamChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SpawnPointHandlerModule extends MatchModule implements Listener {

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        SpawnPoint spawnPoint = event.getTeam().getSpawnPoints().get(0);
        event.getPlayerContext().getPlayer().teleport(spawnPoint.getLocation());
    }
}
