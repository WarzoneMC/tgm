package com.minehut.tgm.api;

import com.minehut.teamapi.client.TeamClient;
import com.minehut.teamapi.models.serverBound.Heartbeat;
import com.minehut.tgm.TGM;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class ApiManager {
    public ApiManager() {

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(TGM.get(), new Runnable() {
            @Override
            public void run() {
                List<ObjectId> players = new ArrayList<>();
                for (PlayerContext playerContext : TGM.get().getPlayerManager().getPlayers()) {
                    try {
                        players.add(new ObjectId(playerContext.getUserProfile().getId()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int maxPlayers = 0;
                int spectatorCount = 0;
                int playerCount = 0;
                for (MatchTeam matchTeam : TGM.get().getModule(TeamManagerModule.class).getTeams()) {
                    if (matchTeam.isSpectator()) {
                        spectatorCount += matchTeam.getMembers().size();
                        continue;
                    }

                    maxPlayers += matchTeam.getMax();
                    playerCount += matchTeam.getMembers().size();
                }
                Heartbeat heartbeat = new Heartbeat(
                        TGM.get().getConfig().getString("server.name"),
                        players,
                        playerCount,
                        spectatorCount,
                        maxPlayers,
                        TGM.get().getMatchManager().getMatch().getMapContainer().getMapInfo().getName(),
                        TGM.get().getMatchManager().getMatch().getMapContainer().getMapInfo().getGametype().getName()
                );
                TGM.get().getTeamClient().heartbeat(heartbeat);
            }
        }, 20L, 20L);
    }
}
