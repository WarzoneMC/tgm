package com.minehut.tgm.match;

import com.minehut.tgm.TGM;
import com.minehut.tgm.map.*;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
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

    @Getter @Setter private MapContainer forcedNextMap = null;

    public MatchManager(FileConfiguration fileConfiguration) {
        mapLibrary = new MapLibrary(fileConfiguration, new MapLoaderImpl());
        mapLibrary.refreshMaps();

        mapRotation = new MapRotationFile(fileConfiguration, mapLibrary);
    }

    public void startMatch() {
        match.enable();
    }

    public void endMatch(MatchTeam winningTeam) {
        List<MatchTeam> losers = new ArrayList<>();
        for (MatchTeam matchTeam : TGM.get().getModule(TeamManagerModule.class).getTeams()) {
            if (!matchTeam.isSpectator() && matchTeam != winningTeam) {
                losers.add(matchTeam);
            }
        }
        Bukkit.getPluginManager().callEvent(new MatchResultEvent(match, winningTeam, losers));

        match.disable();
    }

    public void cycleNextMatch() throws IOException {
        //find a new map to cycle to.
        MapContainer mapContainer = forcedNextMap;
        if (mapContainer == null) {
            mapContainer = mapRotation.cycle();
        }
        forcedNextMap = null;

        //create the new world under a random uuid in the matches folder.
        UUID matchUuid = UUID.randomUUID();
        FileUtils.copyDirectory(mapContainer.getSourceFolder(), new File("matches/" + matchUuid.toString()));
        WorldCreator worldCreator = new WorldCreator("matches/" + matchUuid.toString());
        worldCreator.generator(new NullChunkGenerator());
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

        //transport all players to the new world so we can unload the old one.
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(world.getSpawnLocation());
        }

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
            Bukkit.unloadWorld(oldMatch.getWorld(), false);
            FileUtils.deleteDirectory(worldFolder);
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
