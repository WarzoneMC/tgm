package network.warzone.tgm.map;

import com.google.common.io.Files;
import network.warzone.tgm.TGM;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
@Getter
public class MapRotationFile implements MapRotation {
    private final MapLibrary mapLibrary;
    private int current = 0;
    private List<MapContainer> rotation = new ArrayList<>();
    private File rotationFile;
    private File rotationInfoFile;

    public MapRotationFile(MapLibrary mapLibrary) {
        this.mapLibrary = mapLibrary;
        this.rotationFile = new File(TGM.get().getConfig().getString("rotation"));

        this.rotationInfoFile = new File(TGM.get().getDataFolder().getAbsolutePath() + "/rotation_info.json");

        refresh();

        if (this.rotationInfoFile.exists()) {
            try {
                int newCurrent = TGM.get().getGson().fromJson(new FileReader(this.rotationInfoFile), MapRotationInfo.class).getPosition();
                if(isValidIndex(newCurrent)) this.current = newCurrent;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    private void updateMapRotationInfo() {
        try {
            Writer writer = new FileWriter(this.rotationInfoFile);
            TGM.get().getGson().toJson(new MapRotationInfo(this.current), writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MapContainer cycle() {
        MapContainer currentMap = rotation.get(current);
        updateMapRotationInfo();
        current++;
        if(current >= rotation.size()) current = 0;
        return currentMap;
    }

    private boolean isValidIndex(int newCurrent) {
        return (newCurrent >= 0 && newCurrent < rotation.size());
    }

    @Override
    public MapContainer getNext() {
        if (rotation.size() <= current + 1) {
            return rotation.get(0);
        } else {
            return rotation.get(current + 1);
        }
    }

    @Override
    public void refresh() {
        rotation.clear();
        try {
            List<String> lines = Files.readLines(rotationFile, Charset.defaultCharset());
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

    @Override
    public List<MapContainer> getMaps() {
        return rotation;
    }
}
