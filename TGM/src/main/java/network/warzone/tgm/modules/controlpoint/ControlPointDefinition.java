package network.warzone.tgm.modules.controlpoint;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class ControlPointDefinition {

    private final String name;
    private final int maxProgress;
    private final int pointsPerTick;

}
