package network.warzone.tgm.api;

import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.map.MapInfo;
import network.warzone.tgm.map.ParsedTeam;
import network.warzone.tgm.match.MatchLoadEvent;
import network.warzone.tgm.match.MatchResultEvent;
import network.warzone.tgm.modules.StatsModule;
import network.warzone.tgm.modules.chat.ChatModule;
import network.warzone.tgm.modules.death.DeathInfo;
import network.warzone.tgm.modules.death.DeathModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.player.event.PlayerXPEvent;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.warzoneapi.client.http.HttpClient;
import network.warzone.warzoneapi.models.*;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class ApiManager implements Listener {

    private ObjectId serverId;
    private MatchInProgress matchInProgress;

    private DeathModule deathModule;

    public ApiManager() {
        this.serverId = new ObjectId();
        TGM.registerEvents(this);

        Set<String> players = new HashSet<>();
        Set<String> playerNames = new HashSet<>();

        if (TGM.get().getTeamClient() instanceof HttpClient) Bukkit.getScheduler().runTaskTimerAsynchronously(TGM.get(), () -> {
            players.clear();
            playerNames.clear();

            for (PlayerContext playerContext : TGM.get().getPlayerManager().getPlayers()) {
                try {
                    players.add(playerContext.getUserProfile().getId().toString());
                    playerNames.add(playerContext.getUserProfile().getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            int maxPlayers = Bukkit.getMaxPlayers();
            int spectatorCount = 0;
            int playerCount = Bukkit.getOnlinePlayers().size();
            for (MatchTeam matchTeam : TGM.get().getModule(TeamManagerModule.class).getTeams()) {
                if (matchTeam.isSpectator()) {
                    spectatorCount += matchTeam.getMembers().size();
                }
            }
            Heartbeat heartbeat = new Heartbeat(serverId,
                    TGM.get().getConfig().getString("server.name"),
                    TGM.get().getConfig().getString("server.id"),
                    Bukkit.getMotd(),
                    players,
                    playerNames,
                    playerCount,
                    spectatorCount,
                    maxPlayers,
                    TGM.get().getMatchManager().getMatch().getMapContainer().getMapInfo().getName(),
                    TGM.get().getMatchManager().getMatch().getMapContainer().getMapInfo().getGametype().getName()
            );
            TGM.get().getTeamClient().heartbeat(heartbeat);
        }, 40L, 20L);
    }

    @EventHandler
    public void onMatchResult(MatchResultEvent event) {
        if (isStatsDisabled()) return;
        try {
            List<String> winners = new ArrayList<>();
            if (event.getWinningTeam() != null) {
                for (PlayerContext playerContext : event.getWinningTeam().getMembers()) {
                    winners.add(playerContext.getUserProfile().getId().toString());
                    playerContext.getUserProfile().addWin();
                    Bukkit.getPluginManager().callEvent(new PlayerXPEvent(playerContext, UserProfile.XP_PER_WIN, playerContext.getUserProfile().getXP() - UserProfile.XP_PER_WIN, playerContext.getUserProfile().getXP()));
                }
            }

            List<String> losers = new ArrayList<>();
            for (MatchTeam matchTeam : event.getLosingTeams()) {
                for (PlayerContext playerContext : matchTeam.getMembers()) {
                    losers.add(playerContext.getUserProfile().getId().toString());
                    playerContext.getUserProfile().addLoss();
                    Bukkit.getPluginManager().callEvent(new PlayerXPEvent(playerContext, UserProfile.XP_PER_LOSS, playerContext.getUserProfile().getXP() - UserProfile.XP_PER_LOSS, playerContext.getUserProfile().getXP()));
                }
            }

            TeamManagerModule teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
            List<TeamMapping> teamMappings = new ArrayList<>();
            for (MatchTeam matchTeam : teamManagerModule.getTeams()) {
                if (matchTeam.isSpectator()) continue;

                for (PlayerContext playerContext : matchTeam.getMembers()) {
                    teamMappings.add(new TeamMapping(matchTeam.getId(), playerContext.getUserProfile().getId().toString()));
                }
            }
            Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                MatchFinishPacket matchFinishPacket = new MatchFinishPacket(
                        matchInProgress.getId(),
                        matchInProgress.getMap(),
                        event.getMatch().getStartedTime(),
                        event.getMatch().getFinishedTime(),
                        TGM.get().getModule(ChatModule.class).getChatLog(),
                        winners,
                        losers,
                        event.getWinningTeam() != null ? event.getWinningTeam().getId() : null,
                        teamMappings);
                TGM.get().getTeamClient().finishMatch(matchFinishPacket);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @EventHandler
    public void onMatchLoad(MatchLoadEvent event) {
        if (isStatsDisabled()) return;
        try {
            deathModule = event.getMatch().getModule(DeathModule.class);

            MapInfo mapInfo = event.getMatch().getMapContainer().getMapInfo();
            List<Team> teams = new ArrayList<>();
            for (ParsedTeam parsedTeam : mapInfo.getTeams()) {
                teams.add(new Team(parsedTeam.getId(), parsedTeam.getAlias(), parsedTeam.getTeamColor().name(), parsedTeam.getMin(), parsedTeam.getMax()));
            }

            Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                MapLoadResponse mapLoadResponse = TGM.get().getTeamClient().loadmap(new Map(mapInfo.getName(), mapInfo.getVersion(), mapInfo.getAuthors(), mapInfo.getGametype().toString(), teams));
                Bukkit.getLogger().info("Received load map response. Id: " + mapLoadResponse.getMap() + " [" + mapLoadResponse.isInserted() + "]");
                matchInProgress = TGM.get().getTeamClient().loadMatch(new MatchLoadRequest(mapLoadResponse.getMap()));
                Bukkit.getLogger().info("Match successfully loaded [" + matchInProgress.getMap() + "]");
            });

            teams.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onKill(TGMPlayerDeathEvent event) {
        if (isStatsDisabled()) return;
        try {
            DeathInfo deathInfo = deathModule.getPlayer(event.getVictim());

            PlayerContext killed = TGM.get().getPlayerManager().getPlayerContext(deathInfo.player);

            killed.getUserProfile().addDeath();

            String playerItem = deathInfo.player.getInventory().getItemInMainHand().getType().toString();
            String killerItem = deathInfo.item == null ? "" : deathInfo.item.getType().toString();
            String killerId = null;

            if (deathInfo.killer != null) {
                PlayerContext context = TGM.get().getPlayerManager().getPlayerContext(deathInfo.killer);
                if (context == null) return;
                context.getUserProfile().addKill();
                Bukkit.getPluginManager().callEvent(new PlayerXPEvent(context, UserProfile.XP_PER_KILL, context.getUserProfile().getXP() - UserProfile.XP_PER_KILL, context.getUserProfile().getXP()));

                killerId = context.getUserProfile().getId().toString();
            }

            Death death = new Death(killed.getUserProfile().getId().toString(), killerId, playerItem,
                    killerItem, matchInProgress.getMap(), matchInProgress.getId());

            Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> TGM.get().getTeamClient().addKill(death));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isStatsDisabled() {
        return !TGM.get().getConfig().getBoolean("api.stats.enabled") || TGM.get().getModule(StatsModule.class).isStatsDisabled();
    }
}
