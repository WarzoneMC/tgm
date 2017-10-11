package network.warzone.tgm.map;

import com.google.common.io.Files;
import network.warzone.tgm.TGM;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
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

    public MapRotationFile(MapLibrary mapLibrary) {
        this.mapLibrary = mapLibrary;
        this.rotationFile = new File(TGM.get().getConfig().getString("rotation"));
        refresh();
    }

    public MapContainer cycle(boolean initial) {
        if (initial) {
            return rotation.get(0);
        } else {
            if (rotation.size() <= current + 1) {
                current = 0;
            } else {
                current++;
            }
            return rotation.get(current);
        }
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
