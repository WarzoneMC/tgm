package com.minehut.tgm.match;

import com.google.common.io.Files;
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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
        
        try {
            cycleNextMatch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cycleNextMatch() throws IOException {
        MapContainer mapContainer = mapRotation.cycle();
        UUID matchUuid = UUID.randomUUID();
        FileUtils.copyDirectory(mapContainer.getSourceFolder(), new File(matchUuid.toString()));
        WorldCreator worldCreator = new WorldCreator("matches/" + matchUuid.toString());
        worldCreator.generator(new NullChunkGenerator());
        World world = worldCreator.createWorld();

        MatchManifest matchManifest = null;
        try {
            matchManifest = (MatchManifest) mapContainer.getMapInfo().getGametype().getManifest().getConstructors()[0].newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Match createdMatch = new Match(matchUuid, matchManifest.selectModules(), world, mapContainer);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(world.getSpawnLocation());
        }

        //if a match is currently running, unload it.
        if (match != null) {
            File worldFolder = match.getWorld().getWorldFolder();
            Bukkit.unloadWorld(match.getWorld(), false);
            FileUtils.deleteDirectory(worldFolder);
        }

        match = createdMatch;


    }
}
