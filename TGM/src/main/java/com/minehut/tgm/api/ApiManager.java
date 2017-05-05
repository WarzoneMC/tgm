package com.minehut.tgm.api;

import com.minehut.teamapi.models.Death;
import com.minehut.teamapi.models.Map;
import com.minehut.teamapi.models.Heartbeat;
import com.minehut.teamapi.models.Match;
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
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
    public void onMatchResult(MatchResultEvent event) {
        List<String> winners = new ArrayList<>();
        for (PlayerContext playerContext : event.getWinningTeam().getMembers()) {
            winners.add(playerContext.getUserProfile().getId());
        }

        List<String> losers = new ArrayList<>();
        for (MatchTeam matchTeam : event.getLosingTeams()) {
            for (PlayerContext playerContext : matchTeam.getMembers()) {
                losers.add(playerContext.getUserProfile().getId());
            }
        }
        Match match = new Match(
                currentMap.toString(),
                event.getMatch().getStartedTime(),
                event.getMatch().getFinishedTime(),
                TGM.get().getModule(ChatModule.class).getChatLog(),
                winners,
                losers,
                event.getWinningTeam().getId());
        TGM.get().getTeamClient().matchFinish(match);
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
    public void onKill(PlayerDeathEvent event) {
        PlayerContext player = TGM.get().getPlayerManager().getPlayerContext(event.getPlayer()); //dead

        String playerItem = "";
        if (event.getPlayer().getItemInHand() != null) {
            playerItem = event.getPlayer().getItemInHand().getType().toString();
        }

        String killerItem = "";
        String killerId = null;
        if (event instanceof PlayerDeathByPlayerEvent) {
            killerId = TGM.get().getPlayerManager().getPlayerContext(((PlayerDeathByPlayerEvent) event).getCause()).getUserProfile().getId();
            if (((PlayerDeathByPlayerEvent) event).getCause().getItemInHand() != null) {
                killerItem = ((PlayerDeathByPlayerEvent) event).getCause().getItemInHand().getType().toString();
            }
        }

        Death death = new Death(player.getUserProfile().getId(), killerId, playerItem,
                killerItem, currentMap.toString());
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), new Runnable() {
            @Override
            public void run() {
                TGM.get().getTeamClient().addKill(death);
            }
        });
    }
}
