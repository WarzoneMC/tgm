package com.minehut.tgm.api;

import com.minehut.teamapi.models.*;
import com.minehut.tgm.TGM;
import com.minehut.tgm.damage.grave.event.PlayerDeathByPlayerEvent;
import com.minehut.tgm.damage.grave.event.PlayerDeathEvent;
import com.minehut.tgm.map.MapInfo;
import com.minehut.tgm.map.ParsedTeam;
import com.minehut.tgm.match.MatchLoadEvent;
import com.minehut.tgm.match.MatchResultEvent;
import com.minehut.tgm.modules.ChatModule;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class ApiManager implements Listener {
    private ObjectId serverId;
    @Getter private MatchInProgress matchInProgress;

    public ApiManager() {
        this.serverId = new ObjectId();
        TGM.registerEvents(this);

        Bukkit.getScheduler().runTaskTimerAsynchronously(TGM.get(), new Runnable() {
            @Override
            public void run() {
                List<String> players = new ArrayList<>();
                for (PlayerContext playerContext : TGM.get().getPlayerManager().getPlayers()) {
                    try {
                        players.add(playerContext.getUserProfile().getId().toString());
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
                        TGM.get().getMatchManager().getMatch().getMapContainer().getMapInfo().getGametype().getName(),
                        serverId
                );
                TGM.get().getTeamClient().heartbeat(heartbeat);
            }
        }, 20L, 20L);
    }

    @EventHandler
    public void onMatchResult(MatchResultEvent event) {
        if (isStatsDisabled()) {
            return;
        }

        List<String> winners = new ArrayList<>();
        for (PlayerContext playerContext : event.getWinningTeam().getMembers()) {
            winners.add(playerContext.getUserProfile().getId().toString());
        }

        List<String> losers = new ArrayList<>();
        for (MatchTeam matchTeam : event.getLosingTeams()) {
            for (PlayerContext playerContext : matchTeam.getMembers()) {
                losers.add(playerContext.getUserProfile().getId().toString());
            }
        }

        TeamManagerModule teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        List<TeamMapping> teamMappings = new ArrayList<>();
        for (MatchTeam matchTeam : teamManagerModule.getTeams()) {
            if(matchTeam.isSpectator()) continue;

            for (PlayerContext playerContext : matchTeam.getMembers()) {
                teamMappings.add(new TeamMapping(matchTeam.getId(), playerContext.getUserProfile().getId().toString()));
            }
        }

        MatchFinishPacket matchFinishPacket = new MatchFinishPacket(
                matchInProgress.getId(),
                matchInProgress.getMap(),
                event.getMatch().getStartedTime(),
                event.getMatch().getFinishedTime(),
                TGM.get().getModule(ChatModule.class).getChatLog(),
                winners,
                losers,
                event.getWinningTeam().getId(),
                teamMappings);
        TGM.get().getTeamClient().finishMatch(matchFinishPacket);
    }

    @EventHandler
    public void onMatchLoad(MatchLoadEvent event) {
        if (isStatsDisabled()) {
            return;
        }

        MapInfo mapInfo = event.getMatch().getMapContainer().getMapInfo();
        List<com.minehut.teamapi.models.Team> teams = new ArrayList<>();
        for (ParsedTeam parsedTeam : mapInfo.getTeams()) {
            teams.add(new com.minehut.teamapi.models.Team(parsedTeam.getId(), parsedTeam.getAlias(), parsedTeam.getTeamColor().name(), parsedTeam.getMin(), parsedTeam.getMax()));
        }
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), new Runnable() {
            @Override
            public void run() {
                MapLoadResponse mapLoadResponse = TGM.get().getTeamClient().loadmap(new Map(mapInfo.getName(), mapInfo.getVersion(), mapInfo.getAuthors(), mapInfo.getGametype().toString(), teams));
                Bukkit.getLogger().info("Received load map response. Id: " + mapLoadResponse.getMap() + " [" + mapLoadResponse.isInserted() + "]");
                matchInProgress = TGM.get().getTeamClient().loadMatch(new MatchLoadRequest(mapLoadResponse.getMap()));
                Bukkit.getLogger().info("Match successfully loaded [" + matchInProgress.getMap() + "]");
            }
        });
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        if (isStatsDisabled()) {
            return;
        }

        PlayerContext player = TGM.get().getPlayerManager().getPlayerContext(event.getPlayer()); //dead

        String playerItem = "";
        if (event.getPlayer().getItemInHand() != null) {
            playerItem = event.getPlayer().getItemInHand().getType().toString();
        }

        String killerItem = "";
        String killerId = null;
        if (event instanceof PlayerDeathByPlayerEvent) {
            killerId = TGM.get().getPlayerManager().getPlayerContext(((PlayerDeathByPlayerEvent) event).getCause()).getUserProfile().getId().toString();
            if (((PlayerDeathByPlayerEvent) event).getCause().getItemInHand() != null) {
                killerItem = ((PlayerDeathByPlayerEvent) event).getCause().getItemInHand().getType().toString();
            }
        }

        Death death = new Death(player.getUserProfile().getId().toString(), killerId, playerItem,
                killerItem, matchInProgress.getMap(), matchInProgress.getId());
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), new Runnable() {
            @Override
            public void run() {
                TGM.get().getTeamClient().addKill(death);
            }
        });
    }

    public boolean isStatsDisabled() {
        return !TGM.get().getConfig().getBoolean("api.stats.enabled");
    }
}
