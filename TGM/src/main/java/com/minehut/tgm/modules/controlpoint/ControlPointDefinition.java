package com.minehut.tgm.modules.controlpoint;

import com.minehut.tgm.modules.region.Region;
import com.minehut.tgm.modules.region.RegionSave;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ControlPointDefinition {
    @Getter private final String name;
    @Getter private final int maxProgress;
    @Getter private final int pointsPerTick;
}
