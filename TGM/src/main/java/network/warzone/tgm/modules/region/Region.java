package network.warzone.tgm.modules.region;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.List;

public interface Region {
    boolean contains(Location location);

    Location getCenter();

    List<Block> getBlocks();
}
