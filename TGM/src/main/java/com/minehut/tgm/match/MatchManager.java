package com.minehut.tgm.match;

import com.minehut.tgm.map.*;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by luke on 4/27/17.
 */
public class MatchManager {
    @Getter private MapLibrary mapLibrary;
    @Getter private MapRotation mapRotation;
    @Getter private Match match = null;

    public MatchManager(FileConfiguration fileConfiguration) {
        mapLibrary = new MapLibrary(fileConfiguration, new MapLoaderImpl());
        mapLibrary.refreshMaps();

        mapRotation = new MapRotationFile(fileConfiguration, mapLibrary);
    }

    public void cycleNextMatch() throws IOException {
        //find a new map to cycle to.
        MapContainer mapContainer = mapRotation.cycle();

        //create the new world under a random uuid in the matches folder.
        UUID matchUuid = UUID.randomUUID();
        FileUtils.copyDirectory(mapContainer.getSourceFolder(), new File(matchUuid.toString()));
        WorldCreator worldCreator = new WorldCreator("matches/" + matchUuid.toString());
        worldCreator.generator(new NullChunkGenerator());
        World world = worldCreator.createWorld();

        //parse locations now that we have the world object.
        mapContainer.parseLocations(world);

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

        //create and load the match.
        Match createdMatch = new Match(matchUuid, matchManifest, world, mapContainer);
        createdMatch.load();

        //transport all players to the new world so we can unload the old one.
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(world.getSpawnLocation());
        }

        //if a match is currently running, unload it.
        if (match != null) {
            match.unload();
            File worldFolder = match.getWorld().getWorldFolder();
            Bukkit.unloadWorld(match.getWorld(), false);
            FileUtils.deleteDirectory(worldFolder);
        }

        //we are done with the old match, set the created one as the current.
        match = createdMatch;
    }
}
