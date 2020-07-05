package network.warzone.tgm.map;

import com.google.gson.stream.JsonReader;
import lombok.Getter;
import network.warzone.tgm.TGM;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Getter
public class MapRotationFile {

    private final MapLibrary mapLibrary;
    private final File rotationFile;
    private List<Rotation> rotationLibrary;
    private final Map<String, Integer> indexes;

    private Rotation rotation;
    private int current = 0;

    public MapRotationFile(MapLibrary mapLibrary) {
        this.mapLibrary = mapLibrary;
        this.rotationFile = new File(TGM.get().getConfig().getString("rotation"));
        this.rotationLibrary = new ArrayList<>();
        this.indexes = new HashMap<>();
    }

    public MapContainer cycle(boolean initial) {
        current = (current + (initial ? 0 : 1)) % rotation.getMaps().size();

        if (!rotation.isDefault() && current == rotation.getMaps().size() - 1) {
            MapContainer nextMap = rotation.getMaps().get(current);
            indexes.remove(rotation.getName());
            rotation = getRotationForPlayerCount(Bukkit.getOnlinePlayers().size());
            current = loadRotationPosition(-1);

            return nextMap;
        }

        saveRotationPosition(current);
        return rotation.getMaps().get(current);
    }

    public MapContainer getNext() {
        return rotation.getMaps().get((current + 1) % rotation.getMaps().size());
    }

    public void refresh() {
        // Load rotation files

        if (!rotationFile.exists()) {
            this.rotationLibrary = Collections.singletonList(new Rotation("Preset", true, new RotationRequirement(0, 999999), this.mapLibrary.getMaps()));
            this.rotation = this.rotationLibrary.get(0);

            this.current = 0;
            return;
        }

        try {
            JsonReader reader = new JsonReader(new FileReader(this.rotationFile));
            Rotation[] rotationList = TGM.get().getGson().fromJson(reader, Rotation[].class);

            this.rotationLibrary = Arrays.asList(rotationList);

            rotation = this.rotationLibrary.stream().filter(Rotation::isDefault).findFirst().orElseThrow(() -> new IllegalArgumentException("No default rotation present."));
            current = this.loadRotationPosition(0);
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public boolean hasRotation(String name) {
        return rotationLibrary.stream().anyMatch(rot -> rot.getName().equalsIgnoreCase(name));
    }

    public void setRotation(String name) {
        Rotation newRotation = rotationLibrary.stream().filter(rot -> rot.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        if (newRotation == null) return;

        rotation = newRotation;
        current = loadRotationPosition( -1);
    }

    public List<MapContainer> getMaps() {
        return rotation.getMaps();
    }

    public int loadRotationPosition(int defaultValue) {
        return indexes.getOrDefault(rotation.getName(), defaultValue);
    }

    public void saveRotationPosition(int index) {
        indexes.put(rotation.getName(), index);
    }

    public Rotation getDefaultRotation() {
        return this.rotationLibrary.stream()
                .filter(Rotation::isDefault)
                .findFirst()
                .orElse(null);
    }

    public Rotation getRotationForPlayerCount(int playerCount) {
        return rotationLibrary.stream()
                .filter(Rotation::isDefault)
                .filter(rotation -> rotation.getRequirements().getMin() <= playerCount && rotation.getRequirements().getMax() >= playerCount)
                .findFirst()
                .orElseGet(this::getDefaultRotation);
    }
}
