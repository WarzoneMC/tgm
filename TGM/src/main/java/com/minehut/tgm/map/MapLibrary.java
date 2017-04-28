package com.minehut.tgm.map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
public class MapLibrary {
    @Getter private final List<MapContainer> maps = new ArrayList<>();
    @Getter private final List<File> sources = new ArrayList<>();
    @Getter private final MapLoader mapLoader;

    public MapLibrary(FileConfiguration fileConfiguration, MapLoader mapLoader) {
        for (Object o : fileConfiguration.getConfigurationSection("map").getList("sources")) {
            FileConfiguration sourceConfig = (FileConfiguration) o;
            sources.add(new File(sourceConfig.getString("path")));
        }

        this.mapLoader = mapLoader;
    }

    public void refreshMaps() {
        maps.clear();
        for (File source : sources) {
            for (MapContainer mapContainer : mapLoader.loadMaps(source)) {
                maps.add(mapContainer);
            }
        }
    }
}
