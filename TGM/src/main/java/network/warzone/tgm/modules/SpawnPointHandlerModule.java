package network.warzone.tgm.modules;

import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.gametype.GameType;
import network.warzone.tgm.map.spawnpoints.SpawnPoint;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchManager;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.flag.FlagRespawnBlockEvent;
import network.warzone.tgm.modules.kit.KitEditorModule;
import network.warzone.tgm.modules.kit.classes.GameClass;
import network.warzone.tgm.modules.kit.classes.GameClassModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.event.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.player.event.TGMPlayerRespawnEvent;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.Players;
import network.warzone.tgm.util.menu.KitEditorMenu;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.lang.ref.WeakReference;
import java.util.*;

import static network.warzone.tgm.util.ColorConverter.format;

@Getter
public class SpawnPointHandlerModule extends MatchModule implements Listener {
    private WeakReference<Match> match;
    private TeamManagerModule teamManagerModule;
    private SpectatorModule spectatorModule;
    private GameClassModule gameClassModule;
    private KitEditorModule kitEditorModule;
    private StatsModule statsModule;

    private Map<PlayerContext,MatchTeam> spawning;
    private Map<MatchTeam,Integer> respawnRestrictions;
    private BukkitTask task;

    @Override
    public void load(Match match) {
        this.match = new WeakReference<>(match);
        this.teamManagerModule = match.getModule(TeamManagerModule.class);
        this.spectatorModule = match.getModule(SpectatorModule.class);
        this.gameClassModule = match.getModule(GameClassModule.class);
        this.kitEditorModule = match.getModule(KitEditorModule.class);
        this.statsModule = match.getModule(StatsModule.class);

        this.spawning = new HashMap<>();
        this.respawnRestrictions = new HashMap<>();
        List<MatchTeam> allTeams = teamManagerModule.getTeams();
        for (MatchTeam team : allTeams) {
            this.respawnRestrictions.put(team,0);
        }
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (event.isCancelled()) return;
        MatchTeam team = event.getTeam();
        if (TGM.get().getMatchManager().getMatch().getMatchStatus() == MatchStatus.MID) {
            int restrictions = respawnRestrictions.get(team);
            if (restrictions > 0) {
                PlayerContext context = event.getPlayerContext();
                this.spawning.put(context, team);
                Player player = context.getPlayer();
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setGameMode(GameMode.SPECTATOR);
                player.setVelocity(new Vector());
                return;
            }
            spawnPlayer(event.getPlayerContext(), team, true, true);
        }
        //player is joining the server
        else if (event.getOldTeam() == null) {
            spawnPlayer(event.getPlayerContext(), team, true, true);
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

    @EventHandler
    public void onFlagRestriction(FlagRespawnBlockEvent event) {
        MatchTeam team = event.getAffectedTeam();
        boolean canRespawn = event.isCanRespawn();
        int previousRestriction = respawnRestrictions.get(team);
        if(canRespawn){
            respawnRestrictions.put(team,Math.max(0,previousRestriction-1));
        } else {
            respawnRestrictions.put(team,previousRestriction+1);
        }
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
                    matchTeam.getKits().forEach(kit -> kit.apply(playerContext.getPlayer(), matchTeam));
                } else if (reset) {
                    gameClassModule.performSwitch(playerContext.getPlayer());
                    GameClass gameClass = gameClassModule.getGameClass(gameClassModule.getCurrentClass(playerContext.getPlayer()));
                    if (gameClass != null) gameClass.apply(playerContext.getPlayer(), matchTeam.getColor());
                    playerContext.getPlayer().updateInventory();
                }
                playerContext.getPlayer().setFireTicks(-20);  // Weird lava bug
            }, 1L);  // Delay by 1 tick to prevent missing armor points bug
        } else
            Bukkit.getScheduler().runTaskLater(TGM.get(), () -> {
                playerContext.getPlayer().setFlying(false);
                playerContext.getPlayer().setAllowFlight(false);

                if (matchTeam.isSpectator()) {
                    spectatorModule.applySpectatorKit(playerContext);
                    matchTeam.getKits().forEach(kit -> kit.apply(playerContext.getPlayer(), matchTeam));
                } else if (reset) {
                    if (KitEditorModule.isEnabled() && KitEditorModule.isKitEditable() && kitEditorModule.getEditorMenus().containsKey(playerContext.getPlayer().getUniqueId())) {
                        KitEditorMenu kitEditorMenu = kitEditorModule.getEditorMenus().get(playerContext.getPlayer().getUniqueId());
                        kitEditorMenu.getKit().apply(playerContext.getPlayer(), matchTeam);
                    } else {
                        matchTeam.getKits().forEach(kit -> kit.apply(playerContext.getPlayer(), matchTeam));
                    }
                    playerContext.getPlayer().updateInventory();
                }
                playerContext.getPlayer().setFireTicks(-20); // Weird lava bug
            }, 1L); // Delay by 1 tick to prevent missing armor points bug
        if (statsModule != null) {
            statsModule.setTGMLevel(playerContext);
        }
    }

    @Override
    public void enable() {
        // There is no possibility of respawn restrictions at match start
        for (MatchTeam matchTeam : TGM.get().getModule(TeamManagerModule.class).getTeams()) {
            if (!matchTeam.isSpectator()) {
                for (PlayerContext player : matchTeam.getMembers()) {
                    spawnPlayer(player, matchTeam, true, true);
                }
            }
        }

        task = Bukkit.getScheduler().runTaskTimer(TGM.get(), () -> {
            for (Map.Entry<PlayerContext, MatchTeam> spawnEntry : this.spawning.entrySet()) {
                try {
                    PlayerContext player = spawnEntry.getKey();
                    MatchTeam team = spawnEntry.getValue();

                    if (!teamManagerModule.getTeam(player.getPlayer()).equals(team)) {
                        this.spawning.remove(player);
                        continue;
                    }

                    int restrictions = respawnRestrictions.get(team);
                    if (restrictions > 0) {
                        player.getPlayer().sendTitle(format("&a&lWaiting..."), format("&eYou will spawn when the flag is dropped."), 0, 20, 0);
                        continue;
                    }
                    this.spawning.remove(player);
                    spawnPlayer(player, team, true, true);
                } catch (Exception ignored) {
                }
            }
        }, 0L, 1L);
    }

    @Override
    public void disable() {
        task.cancel();
    }

    public SpawnPoint getTeamSpawn(MatchTeam matchTeam) {
        int i = (int) (Math.random() * (matchTeam.getSpawnPoints().size()));
        return matchTeam.getSpawnPoints().get(i);
    }
}
