package network.warzone.tgm.modules.controlpoint;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ControlPointDefinition {
    @Getter private final String name;
    @Getter private final int maxProgress;
    @Getter private final int pointsPerTick;
}
