package com.minehut.tgm.modules;

import com.minehut.tgm.TGM;
import com.minehut.tgm.map.SpawnPoint;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.match.MatchStatus;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamChangeEvent;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import com.minehut.tgm.util.Players;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class SpawnPointHandlerModule extends MatchModule implements Listener {
    @Getter private TeamManagerModule teamManagerModule;
    @Getter private SpectatorModule spectatorModule;

    @Override
    public void load(Match match) {
        this.teamManagerModule = match.getModule(TeamManagerModule.class);
        this.spectatorModule = match.getModule(SpectatorModule.class);
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (TGM.get().getMatchManager().getMatch().getMatchStatus() == MatchStatus.MID) {
            spawnPlayer(event.getPlayerContext(), event.getTeam(), true);
        }
        //player is joining the server
        else if (event.getOldTeam() == null) {
            spawnPlayer(event.getPlayerContext(), event.getTeam(), true);
        }
        //player is swapping teams pre/post match.
        else {
            //we don't need to teleport them in this case. Let them stay in their position.
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(event.getPlayer());
        MatchTeam matchTeam = teamManagerModule.getTeam(event.getPlayer());
        event.setRespawnLocation(getTeamSpawn(matchTeam).getLocation());

        Bukkit.getScheduler().runTaskLater(TGM.get(), new Runnable() {
            @Override
            public void run() {
                spawnPlayer(playerContext, matchTeam, false);
            }
        }, 0L);
    }

    private void spawnPlayer(PlayerContext playerContext, MatchTeam matchTeam, boolean teleport) {
        Players.reset(playerContext.getPlayer(), true);

        if (matchTeam.isSpectator()) {
            spectatorModule.applySpectatorKit(playerContext);
            if (teleport) {
                playerContext.getPlayer().teleport(getTeamSpawn(matchTeam).getLocation());
            }
        } else {
            matchTeam.getKits().forEach(kit -> kit.apply(playerContext.getPlayer(), matchTeam));
            playerContext.getPlayer().updateInventory();

            if (teleport) {
                playerContext.getPlayer().teleport(getTeamSpawn(matchTeam).getLocation());
                playerContext.getPlayer().setGameMode(GameMode.SURVIVAL);
            }
        }
    }

    @Override
    public void enable() {
        for (MatchTeam matchTeam : TGM.get().getModule(TeamManagerModule.class).getTeams()) {
            if (!matchTeam.isSpectator()) {
                for (PlayerContext player : matchTeam.getMembers()) {
                    spawnPlayer(player, matchTeam, true);
                }
            }
        }
    }

    private SpawnPoint getTeamSpawn(MatchTeam matchTeam) {
        //todo: actually randomize spawn points instead of grabbing first one.
        return matchTeam.getSpawnPoints().get(0);
    }
}
