package network.warzone.tgm.map;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Rotation {
    @Getter private final String name;
    @Getter private final boolean isDefault;
    @Getter private final RotationRequirement requirements;
    private final List<MapContainer> baseMaps;
    private List<MapContainer> activeMaps;

    public Rotation(final String name, final boolean isDefault, final RotationRequirement requirements, List<MapContainer> baseMaps) {
        this(name, isDefault, requirements, baseMaps, false);
    }

    public Rotation(final String name, final boolean isDefault, final RotationRequirement requirements, List<MapContainer> baseMaps, final boolean initialShuffle) {
        this.name = name;
        this.isDefault = isDefault;
        this.requirements = requirements;
        this.baseMaps = baseMaps;
        this.activeMaps = new ArrayList<>(baseMaps);
        if (initialShuffle) this.shuffleMaps();
    }

    public List<MapContainer> getMaps() {
        return this.activeMaps;
    }

    public void shuffleMaps() {
        Collections.shuffle(this.activeMaps);
    }

    public void unshuffleMaps() {
        this.activeMaps = new ArrayList<>(this.baseMaps);
    }
}
