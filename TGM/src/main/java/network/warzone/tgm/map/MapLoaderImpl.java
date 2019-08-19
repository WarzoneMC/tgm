package network.warzone.tgm.map;

import com.google.gson.stream.JsonReader;
import network.warzone.tgm.TGM;

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

        File[] children = folder.listFiles();
        if (children != null) for (File child : children) {
            if (child.isDirectory()) {
                if (isMapFolder(child)) {
                    File mapJsonFile = new File(child, "map.json");
                    try {
                        JsonReader reader = new JsonReader(new FileReader(mapJsonFile));
                        MapInfo mapInfo = TGM.get().getGson().fromJson(reader, MapInfo.class);
                        maps.add(new MapContainer(child, mapInfo));
                    } catch (Exception e) {
                        TGM.get().getLogger().warning("Failed to load map " + child.getName());
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
