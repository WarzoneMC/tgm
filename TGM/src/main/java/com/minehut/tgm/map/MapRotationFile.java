package com.minehut.tgm.map;

import com.google.common.io.Files;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
public class MapRotationFile implements MapRotation {
    @Getter private int current = 0;
    @Getter private List<MapContainer> rotation = new ArrayList<>();

    public MapRotationFile(FileConfiguration fileConfiguration, MapLibrary mapLibrary) {
        try {
            List<String> lines = Files.readLines(new File(fileConfiguration.getString("rotation")), Charset.defaultCharset());
            for (String line : lines) {
                for (MapContainer mapContainer : mapLibrary.getMaps()) {
                    if (mapContainer.getMapInfo().getName().equalsIgnoreCase(line)) {
                        rotation.add(mapContainer);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MapContainer cycle() {
        if (rotation.size() >= current + 1) {
            current = 0;
        } else {
            current++;
        }
        return rotation.get(current);
    }
}
