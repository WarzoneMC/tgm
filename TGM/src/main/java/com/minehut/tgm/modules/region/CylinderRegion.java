package com.minehut.tgm.modules.region;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

@AllArgsConstructor
public class CylinderRegion implements Region {
    @Getter private final Location base;
    @Getter private final double radius, height;

    @Override
    public boolean contains(Location location) {
        if (Math.sqrt(location.distanceSquared(base)) <= radius) {
            if (location.getY() >= base.getY() && location.getY() <= base.getY() + height) {
                return true;
            }
        }
        return false;
    }
}
