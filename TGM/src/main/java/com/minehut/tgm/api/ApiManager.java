package com.minehut.tgm.api;

import com.minehut.teamapi.client.TeamClient;
import com.minehut.teamapi.models.Kill;
import com.minehut.teamapi.models.Map;
import com.minehut.teamapi.models.serverBound.Heartbeat;
import com.minehut.tgm.TGM;
import com.minehut.tgm.damage.grave.event.PlayerDeathByPlayerEvent;
import com.minehut.tgm.damage.grave.event.PlayerDeathEvent;
import com.minehut.tgm.map.MapInfo;
import com.minehut.tgm.map.ParsedTeam;
import com.minehut.tgm.match.MatchLoadEvent;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class ApiManager implements Listener {
    private ObjectId currentMap;

    public ApiManager() {
        TGM.registerEvents(this);

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(TGM.get(), new Runnable() {
            @Override
            public void run() {
                List<String> players = new ArrayList<>();
                for (PlayerContext playerContext : TGM.get().getPlayerManager().getPlayers()) {
                    try {
                        players.add(playerContext.getUserProfile().getId());
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
                        TGM.get().getConfig().getString("server.id"),
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

    @EventHandler
    public void onMatchLoad(MatchLoadEvent event) {
        MapInfo mapInfo = event.getMatch().getMapContainer().getMapInfo();
        List<com.minehut.teamapi.models.Team> teams = new ArrayList<>();
        for (ParsedTeam parsedTeam : mapInfo.getTeams()) {
            teams.add(new com.minehut.teamapi.models.Team(parsedTeam.getId(), parsedTeam.getAlias(), parsedTeam.getTeamColor().name(), parsedTeam.getMin(), parsedTeam.getMax()));
        }
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), new Runnable() {
            @Override
            public void run() {
                currentMap = TGM.get().getTeamClient().loadmap(new Map(mapInfo.getName(), mapInfo.getVersion(), mapInfo.getAuthors(), mapInfo.getGametype().toString(), teams));
            }
        });
    }

    @EventHandler
    public void onKill(PlayerDeathByPlayerEvent event) {
        PlayerContext player = TGM.get().getPlayerManager().getPlayerContext(event.getCause()); //dead
        PlayerContext target = TGM.get().getPlayerManager().getPlayerContext(event.getPlayer()); //killer

        String playerItem = "";
        if (event.getPlayer().getItemInHand() != null) {
            playerItem = event.getPlayer().getItemInHand().getType().toString();
        }

        String targetItem = "";
        if (event.getCause().getItemInHand() != null) {
            targetItem = event.getCause().getItemInHand().getType().toString();
        }

        Kill kill = new Kill(player.getUserProfile().getId(),
                target.getUserProfile().getId(), playerItem,
                targetItem, currentMap.toString());
        TGM.get().getTeamClient().addKill(kill);
    }
}
