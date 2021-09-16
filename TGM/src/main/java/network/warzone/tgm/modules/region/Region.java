package network.warzone.tgm.modules.region;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.List;

public interface Region {
    boolean contains(Location location);

    boolean contains(Block block);

    Location getCenter();

    Location getRandomLocation();

    List<Block> getBlocks();

    Location getMin();

    Location getMax();
}
