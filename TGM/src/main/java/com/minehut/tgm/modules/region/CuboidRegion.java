package com.minehut.tgm.modules.region;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;


public class CuboidRegion implements Region {
    @Getter World world;
    @Getter double minX, minY, minZ, maxX, maxY, maxZ;

    public CuboidRegion(World world, Location min, Location max) {
        this.world = world;

        minX = Math.min(min.getX(), max.getX());
        maxX = Math.max(min.getX(), max.getX());

        minY = Math.min(min.getY(), max.getY());
        maxY = Math.max(min.getY(), max.getY());

        minZ = Math.min(min.getZ(), max.getZ());
        maxZ = Math.max(min.getZ(), max.getZ());
    }

    public Location getMin() {
        return new Location(world, minX, minY, minZ);
    }

    public Location getMax() {
        return new Location(world, maxX, maxY, maxZ);
    }

    @Override
    public boolean contains(Location location) {
        return location.toVector().isInAABB(getMin().toVector(), getMax().toVector());
    }
}
