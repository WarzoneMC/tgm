package network.warzone.tgm.map;

import com.google.common.io.Files;
import lombok.Getter;
import network.warzone.tgm.TGM;

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

    private static final File rindex = new File(TGM.get().getDataFolder(), ".rindex");

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
        current = (current + (initial ? 0 : 1)) % rotation.size();
        saveRotationPosition(current);
        return rotation.get(current);
    }

    @Override
    public MapContainer getNext() {
        return rotation.get((current + 1) % rotation.size());
    }

    @Override
    public void refresh() {
        rotation.clear();

        if (!rotationFile.exists()) {
            rotation.addAll(mapLibrary.getMaps());
            this.current = 0;
            return;
        }

        try {
            List<String> lines = Files.readLines(rotationFile, Charset.defaultCharset());
            for (String line : lines) {
                for (MapContainer mapContainer : mapLibrary.getMaps()) {
                    if (mapContainer.getMapInfo().getName().equalsIgnoreCase(line.trim())) {
                        rotation.add(mapContainer);
                    }
                }
            }
            this.current = loadRotationPosition();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<MapContainer> getMaps() {
        return rotation;
    }

    @Override
    public int loadRotationPosition() {
        try {
            return Integer.parseInt(Files.readFirstLine(rindex, Charset.defaultCharset()));
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void saveRotationPosition(int i) {
        int index = i % rotation.size();
        try {
            Files.write(String.valueOf(index), rindex, Charset.defaultCharset());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
