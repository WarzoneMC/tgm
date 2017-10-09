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
import com.minehut.tgm.modules.DeathModule;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.player.event.PlayerXPEvent;
import com.minehut.tgm.user.PlayerContext;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ApiManager implements Listener {

    private ObjectId serverId;
    @Getter private MatchInProgress matchInProgress;

    private DeathModule deathModule;

    public ApiManager() {
        this.serverId = new ObjectId();
        TGM.registerEvents(this);

        Bukkit.getScheduler().runTaskTimerAsynchronously(TGM.get(), () -> {
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
        }, 20L, 20L);
    }

    @EventHandler
    public void onMatchResult(MatchResultEvent event) {
        if (isStatsDisabled()) return;

        List<String> winners = new ArrayList<>();
        for (PlayerContext playerContext : event.getWinningTeam().getMembers()) {
            winners.add(playerContext.getUserProfile().getId().toString());
            playerContext.getUserProfile().addWin();
            Bukkit.getPluginManager().callEvent(new PlayerXPEvent(playerContext, UserProfile.XP_PER_WIN, playerContext.getUserProfile().getXP() - UserProfile.XP_PER_WIN, playerContext.getUserProfile().getXP()));
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

    public void xpEffect(UUID uuid, int before, int ammount, int after) {
        BossBar bar = Bukkit.createBossBar(ChatColor.GREEN.toString() + before + "/" + after + ChatColor.DARK_GREEN + " +" + ammount, BarColor.GREEN, BarStyle.SEGMENTED_12);
        bar.setProgress(0.5);
        bar.addPlayer(Bukkit.getPlayer(uuid));
        bar.setVisible(true);
        Bukkit.getScheduler().runTaskLater(TGM.get(), () -> {
            bar.setTitle(ChatColor.GREEN.toString() + (before + ammount) + "xp");
            Bukkit.getScheduler().runTaskLater(TGM.get(), () -> {
                bar.setVisible(false);
                bar.removeAll();
            }, 60L);
        }, 60L);
    }

    @EventHandler
    public void onMatchLoad(MatchLoadEvent event) {
        if (isStatsDisabled()) return;
        deathModule = event.getMatch().getModule(DeathModule.class);

        MapInfo mapInfo = event.getMatch().getMapContainer().getMapInfo();
        List<com.minehut.teamapi.models.Team> teams = new ArrayList<>();
        for (ParsedTeam parsedTeam : mapInfo.getTeams()) {
            teams.add(new com.minehut.teamapi.models.Team(parsedTeam.getId(), parsedTeam.getAlias(), parsedTeam.getTeamColor().name(), parsedTeam.getMin(), parsedTeam.getMax()));
        }

        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
            MapLoadResponse mapLoadResponse = TGM.get().getTeamClient().loadmap(new Map(mapInfo.getName(), mapInfo.getVersion(), mapInfo.getAuthors(), mapInfo.getGametype().toString(), teams));
            Bukkit.getLogger().info("Received load map response. Id: " + mapLoadResponse.getMap() + " [" + mapLoadResponse.isInserted() + "]");
            matchInProgress = TGM.get().getTeamClient().loadMatch(new MatchLoadRequest(mapLoadResponse.getMap()));
            Bukkit.getLogger().info("Match successfully loaded [" + matchInProgress.getMap() + "]");
        });
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        if (isStatsDisabled()) return;
        DeathModule module = deathModule.getPlayer(event.getPlayer());

        PlayerContext killed = TGM.get().getPlayerManager().getPlayerContext(module.getPlayer());

        killed.getUserProfile().addDeath();

        String playerItem = event.getPlayer().getInventory().getItemInMainHand() == null ? "" : event.getPlayer().getInventory().getItemInMainHand().getType().toString();
        String killerItem = module.getItem() == null ? "" : module.getItem().getType().toString();
        String killerId = null;

        if (event instanceof PlayerDeathByPlayerEvent) {
            PlayerContext context = TGM.get().getPlayerManager().getPlayerContext(module.getKiller());
            if (context == null) return;
            //int a = context.getUserProfile().getXP();
            context.getUserProfile().addKill();
            Bukkit.getPluginManager().callEvent(new PlayerXPEvent(context, UserProfile.XP_PER_KILL, context.getUserProfile().getXP() - UserProfile.XP_PER_KILL, context.getUserProfile().getXP()));
            //int b = context.getUserProfile().getXP();
            //xpEffect(context.getPlayer().getUniqueId(), a, b - a, 6 * context.getUserProfile().getLevel());

            killerId = context.getUserProfile().getId().toString();
        }

        Death death = new Death(killed.getUserProfile().getId().toString(), killerId, playerItem,
                killerItem, matchInProgress.getMap(), matchInProgress.getId());

        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> TGM.get().getTeamClient().addKill(death));
    }

    public boolean isStatsDisabled() {
        return !TGM.get().getConfig().getBoolean("api.stats.enabled");
    }
}
