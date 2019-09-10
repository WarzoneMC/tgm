package network.warzone.tgm.modules.region;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;


public class CuboidRegion implements Region {
    @Getter private final World world;
    @Getter double minX, minY, minZ, maxX, maxY, maxZ;

    private final Location min;
    private final Location max;

    public CuboidRegion(Location min, Location max) {
        Preconditions.checkArgument(min.getWorld() == max.getWorld(), "region location worlds must match");
        this.world = min.getWorld();
        minX = Math.min(min.getX(), max.getX());
        maxX = Math.max(min.getX(), max.getX());

        minY = Math.min(min.getY(), max.getY());
        maxY = Math.max(min.getY(), max.getY());

        minZ = Math.min(min.getZ(), max.getZ());
        maxZ = Math.max(min.getZ(), max.getZ());

        this.min = new Location(world, minX, minY, minZ);
        this.max = new Location(world, maxX, maxY, maxZ);
    }

    @Override
    public Location getMin() {
        return min;
    }

    @Override
    public Location getMax() {
        return max;
    }

    @Override
    public boolean contains(Location location) {
        return location.toVector().isInAABB(getMin().toVector(), getMax().toVector());
    }

    @Override
    public Location getCenter() {
        Vector v = getMin().toVector().getMidpoint(getMax().toVector());
        return new Location(world, v.getX(), v.getY(), v.getZ());
    }

    @Override
    public List<Block> getBlocks() {
        List<Block> results = new ArrayList<>();
        for (int x = (int) getMinX(); x <= getMaxX(); x++) {
            for (int z = (int) getMinZ(); z <= getMaxZ(); z++) {
                for (int y = (int) getMinY(); y <= getMaxY(); y++) {
                    results.add((new Location(world, x, y, z).getBlock()));
                }
            }
        }
        return results;
    }
}
