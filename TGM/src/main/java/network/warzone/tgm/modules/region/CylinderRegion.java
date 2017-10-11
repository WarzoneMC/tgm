package network.warzone.tgm.modules.region;

import network.warzone.tgm.TGM;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public Location getCenter() {
        return base;
    }

    @Override
    public List<Block> getBlocks() {
        List<Block> results = new ArrayList<>();
        CuboidRegion bound = new CuboidRegion(null, getMin(), getMax());
        for (Block block : bound.getBlocks()) {
            if (contains(new Location(TGM.get().getMatchManager().getMatch().getWorld(), block.getX(), block.getY(), block.getZ()))) results.add(block);
        }
        return results;
    }

    public Location getMin() {
        return new Location(TGM.get().getMatchManager().getMatch().getWorld(), base.getX() - radius, base.getY(), base.getZ() - radius);
    }

    public Location getMax() {
        return new Location(TGM.get().getMatchManager().getMatch().getWorld(), base.getX() + radius, base.getY() + height, base.getZ() + radius);
    }
}
