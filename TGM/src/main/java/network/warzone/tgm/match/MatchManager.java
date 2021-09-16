package network.warzone.tgm.match;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.map.*;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;

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


    private static final ChunkGenerator CHUNK_GENERATOR = new NullChunkGenerator();

    private MapLibrary mapLibrary;
    private MapRotationFile mapRotation;
    private Match match = null;
    private int matchNumber = 0;

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
            if (!matchTeam.isSpectator() && !matchTeam.equals(winningTeam)) {
                losers.add(matchTeam);
            }
        }
        match.disable();
        handleRotationUpdate();

        Bukkit.getPluginManager().callEvent(new MatchResultEvent(match, winningTeam, losers));
    }

    private static void handleRotationUpdate() {
        int playerCount = Bukkit.getOnlinePlayers().size();
        MapRotationFile rotationFile = TGM.get().getMatchManager().getMapRotation();

        if (!rotationFile.getRotation().isDefault()) return;
        Rotation potentialRotation = rotationFile.getRotationForPlayerCount(playerCount);

        if (potentialRotation != rotationFile.getRotation()) {
            System.out.println("Rotation has changed to " + potentialRotation.getName() + " from " + rotationFile.getRotation().getName());
            Bukkit.getOnlinePlayers().forEach(
                    player -> player.sendMessage(
                            ChatColor.GRAY + "The rotation has been updated to " + ChatColor.GOLD + potentialRotation.getName() + ChatColor.GRAY + " to accommodate for the new player size."
                    )
            );

            rotationFile.setRotation(potentialRotation.getName());
        }
    }

    public void cycleNextMatch() {
        //find a new map to cycle to.
        MapContainer mapContainer = forcedNextMap;
        if (mapContainer == null) {
            mapContainer = mapRotation.cycle(matchNumber == 1);
            if (mapContainer == null) {
                System.out.println("No maps could be found in the rotation. Are there any maps?");
                return;
            }
        }
        matchNumber++;
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
        worldCreator.generator(CHUNK_GENERATOR);
        worldCreator.generateStructures(false);

        World world = worldCreator.createWorld();
        world.setAutoSave(false);
        world.setKeepSpawnInMemory(false);
        world.setTicksPerAnimalSpawns(0);
        world.setTicksPerMonsterSpawns(0);
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
        Bukkit.getOnlinePlayers().forEach(player ->
                player.teleport(world.getSpawnLocation()));

        //create and load the match.
        Match createdMatch = new Match(matchUuid, matchManifest, world, mapContainer);
        Match oldMatch = match;
        match = createdMatch;

        createdMatch.load();

        //parse locations now that we have the world object.
        mapContainer.parseWorldDependentContent(world);

        //if a match is currently running, unload it.
        if (oldMatch != null) {
            File worldFolder = oldMatch.getWorld().getWorldFolder();
            oldMatch.getWorld().getPlayers().forEach(player ->
                    player.teleport(world.getSpawnLocation()));

            TGM.get().getLogger().info("Unloading match: " + oldMatch.getUuid().toString() + " (File: " + oldMatch.getWorld().getWorldFolder().getPath() + ")");

            boolean save = TGM.get().getConfig().getBoolean("map.save-matches", false);
            Bukkit.unloadWorld(oldMatch.getWorld(), save);
            if (!save)
                Bukkit.getScheduler().runTaskLaterAsynchronously(TGM.get(), () -> {
                    try {
                        FileUtils.deleteDirectory(worldFolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, 80L); // 4 seconds
        }
    }

    public MapContainer getNextMap() {
        if (forcedNextMap != null) {
            return forcedNextMap;
        } else {
            return mapRotation.getNext();
        }
    }
}
