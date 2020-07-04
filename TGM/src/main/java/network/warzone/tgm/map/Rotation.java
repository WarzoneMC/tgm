package network.warzone.tgm.map;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class Rotation {
    private final String name;
    private final boolean isDefault;
    private final RotationRequirement requirements;
    private final List<MapContainer> maps;
}
