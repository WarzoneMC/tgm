package com.minehut.tgm.map;

import com.google.gson.stream.JsonReader;
import com.minehut.tgm.TGM;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
public class MapLoaderImpl implements MapLoader {

    @Override
    public List<MapContainer> loadMaps(File folder) {
        List<MapContainer> maps = new ArrayList<>();

        for (File child : folder.listFiles()) {
            if (child.isDirectory()) {
                if (isMapFolder(child)) {
                    File mapJsonFile = new File(child, "map.json");
                    try {
                        JsonReader reader = new JsonReader(new FileReader(mapJsonFile));
                        MapInfo mapInfo = TGM.get().getGson().fromJson(reader, MapInfo.class);
                        maps.add(new MapContainer(child, mapInfo));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    //recursively loop through directories
                    for (MapContainer mapContainer : loadMaps(child)) {
                        maps.add(mapContainer);
                    }
                }
            }
        }

        return maps;
    }

    private boolean isMapFolder(File folder) {
        for (File file : folder.listFiles()) {
            if (file.getName().equals("map.json")) {
                return true;
            }
        }
        return false;
    }
}
