package network.warzone.tgm.modules.region;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
public class CuboidRegion implements Region {
    private final World world;
    private final Random random;

    private double minX;
    private double minY;
    private double minZ;
    private double maxX;
    private double maxY;
    private double maxZ;

    private final Location min;
    private final Location max;

    public CuboidRegion(Location min, Location max) {
        Preconditions.checkArgument(min.getWorld() == max.getWorld(), "region location worlds must match");
        this.world = min.getWorld();
        this.random = new Random();

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
    public boolean contains(Block block) {
        return contains(block.getLocation());
    }

    @Override
    public Location getCenter() {
        Vector v = getMin().toVector().getMidpoint(getMax().toVector());
        return new Location(world, v.getX(), v.getY(), v.getZ());
    }

    @Override
    public Location getRandomLocation() {
        double x = getMinX() + (getMaxX() - getMinX()) * random.nextDouble();
        double y = getMinY() + (getMaxY() - getMinY()) * random.nextDouble();
        double z = getMinZ() + (getMaxZ() - getMinZ()) * random.nextDouble();
        return new Location(world, x, y, z);
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
