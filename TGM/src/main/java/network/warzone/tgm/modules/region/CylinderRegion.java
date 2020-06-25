package network.warzone.tgm.modules.region;

import lombok.Getter;
import network.warzone.tgm.TGM;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.NumberConversions;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CylinderRegion implements Region {
    private final Location base;
    private final double radius;
    private final double height;

    private final Location min;
    private final Location max;

    public CylinderRegion(Location base, double radius, double height) {
        this.base = base;
        this.radius = radius;
        this.height = height;

        this.min = new Location(base.getWorld(), base.getX() - radius, base.getY(), base.getZ() - radius);
        this.max = new Location(base.getWorld(), base.getX() + radius, base.getY() + height, base.getZ() + radius);
    }

    @Override
    public boolean contains(Location location) {
        return Math.sqrt(NumberConversions.square(base.getX() - location.getX()) + NumberConversions.square(base.getZ() - location.getZ())) <= radius &&
                location.getY() >= base.getY() &&
                location.getY() <= base.getY() + height;
    }

    @Override
    public boolean contains(Block block) {
        return contains(block.getLocation());
    }

    @Override
    public Location getCenter() {
        return base;
    }

    @Override
    public List<Block> getBlocks() {
        List<Block> results = new ArrayList<>();
        CuboidRegion bound = new CuboidRegion(getMin(), getMax());
        for (Block block : bound.getBlocks()) {
            if (contains(new Location(TGM.get().getMatchManager().getMatch().getWorld(), block.getX(), block.getY(), block.getZ()))) results.add(block);
        }
        return results;
    }

    @Override
    public Location getMin() {
        return this.min;
    }

    @Override
    public Location getMax() {
        return this.max;
    }
}
