package network.warzone.tgm.map;

import lombok.Getter;
import network.warzone.tgm.TGM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Rotation {
    @Getter private final String name;
    @Getter private final boolean isDefault;
    @Getter private final RotationRequirement requirements;
    private final List<String> mapNames;
    private List<MapContainer> baseMaps;
    private List<MapContainer> activeMaps;
    private final boolean shuffle;

    public Rotation(final String name, final boolean isDefault, final RotationRequirement requirements, List<MapContainer> baseMaps, List<String> mapNames) {
        this(name, isDefault, requirements, baseMaps, mapNames, false);
    }

    public Rotation(final String name, final boolean isDefault, final RotationRequirement requirements, List<MapContainer> baseMaps, List<String> mapNames, final boolean shuffle) {
        this.name = name;
        this.isDefault = isDefault;
        this.requirements = requirements;
        this.baseMaps = baseMaps;
        this.mapNames = mapNames;
        this.activeMaps = new ArrayList<>(baseMaps);
        if (shuffle) this.shuffleMaps();
        this.shuffle = shuffle;
    }

    public List<MapContainer> getMaps() {
        return this.activeMaps;
    }

    // Map collection becomes stale if maps are loaded after startup
    public void refresh() {
        this.reloadMaps(); // Reconstruct basemaps
        this.resetMapsToBase(); // Copy base to active
        if (this.shuffle) this.shuffleMaps(); // Shuffle active if necessary
    }

    // In place shuffle of active maps
    public void shuffleMaps() {
        Collections.shuffle(this.activeMaps);
    }

    // Active maps are copied from base maps
    public void resetMapsToBase() {
        this.activeMaps = new ArrayList<>(this.baseMaps);
    }

    // Reconstructs Map collection from map names. This is useful when maps are loaded after startup
    public void reloadMaps() {
        this.baseMaps.clear();

        // If the map name list is null, add all Maps. This will only apply for the default rotation.
        // Deserialized rotations will have a non-null (but possibly empty) collection for mapNames

        if (this.mapNames == null) {
            this.baseMaps.addAll(TGM.get().getMatchManager().getMapLibrary().getMaps());
        } else {
            for (String mapName : this.mapNames) {
                for (MapContainer mapContainer : TGM.get().getMatchManager().getMapLibrary().getMaps()) {
                    if (mapContainer.getMapInfo().getName().equalsIgnoreCase(mapName)) {
                        this.baseMaps.add(mapContainer);
                    }
                }
            }
        }
    }
}
