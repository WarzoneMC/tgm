package network.warzone.tgm.match;

import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.map.*;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by luke on 4/27/17.
 */
@Getter
public class MatchManager {
    private MapLibrary mapLibrary;
    private MapRotation mapRotation;
    private Match match = null;
    private int matchNumber = 0;

    private BukkitTask unloadMatchTask;

    @Setter private MapContainer forcedNextMap = null;

    public MatchManager(FileConfiguration fileConfiguration) {
        mapLibrary = new MapLibrary(fileConfiguration, new MapLoaderImpl());
        mapLibrary.refreshMaps();

        mapRotation = new MapRotationFile(mapLibrary);
    }

    public void startMatch() {
        match.enable();
    }

    public void endMatch(MatchTeam winningTeam) {
        List<MatchTeam> losers = new ArrayList<>();
        for (MatchTeam matchTeam : TGM.get().getModule(TeamManagerModule.class).getTeams()) {
            if (!matchTeam.isSpectator()) {
                matchTeam.getMembers().forEach(playerContext -> {
                    playerContext.getPlayer().setGameMode(GameMode.ADVENTURE);
                    playerContext.getPlayer().setAllowFlight(true);
                    playerContext.getPlayer().setVelocity(playerContext.getPlayer().getVelocity().setY(1.0)); // Weeee!
                    playerContext.getPlayer().setFlying(true);
                });

                if (matchTeam != winningTeam) {
                    losers.add(matchTeam);
                }
            }
        }
        match.disable();

        Bukkit.getPluginManager().callEvent(new MatchResultEvent(match, winningTeam, losers));
    }

    public void cycleNextMatch() {
        Bukkit.getScheduler().runTask(TGM.get(), () -> {
            matchNumber++;

            //find a new map to cycle to.
            MapContainer mapContainer = forcedNextMap;
            if (mapContainer == null) {
                mapContainer = mapRotation.cycle(matchNumber == 1);
            }
            forcedNextMap = null;

            //generate next match's uuid
            UUID matchUuid = UUID.randomUUID();

            try {
                FileUtils.copyDirectory(mapContainer.getSourceFolder(), new File("matches/" + matchUuid.toString()));


            } catch (IOException e) {
                e.printStackTrace();
            }

            //create the new world under a random uuid in the matches folder.
            WorldCreator worldCreator = new WorldCreator("matches/" + matchUuid.toString());
            worldCreator.generator(new NullChunkGenerator());
            //worldCreator.environment(World.Environment.NETHER);
            World world = worldCreator.createWorld();

            /**
             * Initialize a match manifest based on the map's gametype.
             * The match manifest will handle which match modules should
             * be loaded.
             */
            MatchManifest matchManifest = null;
            try {
                matchManifest = (MatchManifest) mapContainer.getMapInfo().getGametype().getManifest().getConstructors()[0].newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //unload the existing match modules before we move any players.
            if (match != null) {
                match.unload();
            }

            // Transport all players to the new world so we can unload the old one.
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.teleport(world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }

            //create and load the match.
            Match createdMatch = new Match(matchUuid, matchManifest, world, mapContainer);
            Match oldMatch = match;
            match = createdMatch;

            createdMatch.load();

            //parse locations now that we have the world object.
            mapContainer.parseWorldDependentContent(world);

            //if a match is currently running, unload it.
            unloadMatchTask = Bukkit.getScheduler().runTaskTimer(TGM.get(), () -> {
                if (oldMatch != null) {
                    try {
                        File worldFolder = oldMatch.getWorld().getWorldFolder();
                        Bukkit.unloadWorld(oldMatch.getWorld(), false);
                        FileUtils.deleteDirectory(worldFolder);

                        unloadMatchTask.cancel();
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }

                unloadMatchTask.cancel();
            }, 30L, 10L);
        });
    }

    public MapContainer getNextMap() {
        if (forcedNextMap != null) {
            return forcedNextMap;
        } else {
            return mapRotation.getNext();
        }
    }
}
