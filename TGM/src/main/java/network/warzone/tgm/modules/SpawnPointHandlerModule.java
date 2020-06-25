package network.warzone.tgm.modules;

import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.gametype.GameType;
import network.warzone.tgm.map.SpawnPoint;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchManager;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.kit.classes.GameClass;
import network.warzone.tgm.modules.kit.classes.GameClassModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.player.event.TGMPlayerRespawnEvent;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.Players;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.lang.ref.WeakReference;

@Getter
public class SpawnPointHandlerModule extends MatchModule implements Listener {
    private WeakReference<Match> match;
    private TeamManagerModule teamManagerModule;
    private SpectatorModule spectatorModule;
    private GameClassModule gameClassModule;

    @Override
    public void load(Match match) {
        this.match = new WeakReference<Match>(match);
        this.teamManagerModule = match.getModule(TeamManagerModule.class);
        this.spectatorModule = match.getModule(SpectatorModule.class);
        gameClassModule = TGM.get().getModule(GameClassModule.class);
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (event.isCancelled()) return;
        if (TGM.get().getMatchManager().getMatch().getMatchStatus() == MatchStatus.MID) {
            spawnPlayer(event.getPlayerContext(), event.getTeam(), true, true);
        }
        //player is joining the server
        else if (event.getOldTeam() == null) {
            spawnPlayer(event.getPlayerContext(), event.getTeam(), true, true);
        }
        //player is swapping teams pre/post match.
        //else {
            //we don't need to teleport them in this case. Let them stay in their position.
        //}
    }

    @EventHandler(ignoreCancelled = true)
    public void onRespawn(TGMPlayerRespawnEvent event) {
        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(event.getPlayer());
        MatchTeam matchTeam = teamManagerModule.getTeam(event.getPlayer());

        spawnPlayer(playerContext, matchTeam, true, false);
    }

    public void spawnPlayer(PlayerContext playerContext, MatchTeam matchTeam, boolean teleport, boolean firstSpawn) {
        boolean reset = firstSpawn || !match.get().getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY);
        Players.reset(playerContext.getPlayer(), true, !reset);

        if (teleport) {
            MatchManager matchManager = TGM.get().getMatchManager();
            GameType gameType = matchManager.getMatch().getMapContainer().getMapInfo().getGametype();

            playerContext.getPlayer().setVelocity(new Vector(0, 0, 0));
            playerContext.getPlayer().setAllowFlight(true);
            playerContext.getPlayer().setFlying(true);
            playerContext.getPlayer().teleport(getTeamSpawn(matchTeam).getLocation());
            if (!matchTeam.isSpectator() && !gameType.equals(GameType.Infected)) playerContext.getPlayer().setGameMode(matchTeam.getGamemode());
        }
        if (gameClassModule != null) {
            Bukkit.getScheduler().runTaskLater(TGM.get(), () -> {
                if (gameClassModule.getCurrentClass(playerContext.getPlayer()) == null) gameClassModule.setCurrentClass(playerContext.getPlayer(), gameClassModule.getDefaultClass());
                if (matchTeam.isSpectator()) {
                    spectatorModule.applySpectatorKit(playerContext);
                } else {
                    if (reset) {
                        gameClassModule.performSwitch(playerContext.getPlayer());
                        GameClass gameClass = gameClassModule.getGameClass(gameClassModule.getCurrentClass(playerContext.getPlayer()));
                        if (gameClass != null) gameClass.apply(playerContext.getPlayer(), matchTeam.getColor());
                        playerContext.getPlayer().updateInventory();
                    }
                }
                playerContext.getPlayer().setFireTicks(-20);  // Weird lava bug
            }, 1L);  // Delay by 1 tick to prevent missing armor points bug
        } else
            Bukkit.getScheduler().runTaskLater(TGM.get(), () -> {
                playerContext.getPlayer().setFlying(false);
                playerContext.getPlayer().setAllowFlight(false);

                if (matchTeam.isSpectator()) {
                    spectatorModule.applySpectatorKit(playerContext);
                }
                if (reset) {
                    matchTeam.getKits().forEach(kit -> kit.apply(playerContext.getPlayer(), matchTeam));
                    playerContext.getPlayer().updateInventory();
                }
                playerContext.getPlayer().setFireTicks(-20);  // Weird lava bug
            }, 1L); // Delay by 1 tick to prevent missing armor points bug
    }

    @Override
    public void enable() {
        for (MatchTeam matchTeam : TGM.get().getModule(TeamManagerModule.class).getTeams()) {
            if (!matchTeam.isSpectator()) {
                for (PlayerContext player : matchTeam.getMembers()) {
                    spawnPlayer(player, matchTeam, true, true);
                }
            }
        }
    }

    public SpawnPoint getTeamSpawn(MatchTeam matchTeam) {
        int i = (int) (Math.random() * (matchTeam.getSpawnPoints().size()));
        return matchTeam.getSpawnPoints().get(i);
    }
}
