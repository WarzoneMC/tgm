package com.minehut.tgm.modules.region;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

@AllArgsConstructor
public class CuboidRegion implements Region {
    @Getter private Location pos1, pos2;

    @Override
    public boolean contains(Location location) {
        return location.toVector().isInAABB(pos1.toVector(), pos2.toVector());
    }
}
