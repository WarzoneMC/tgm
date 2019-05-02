package network.warzone.tgm.modules.region;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.HashMap;

public final class RegionSave {

    private final HashMap<BlockVector, Material> blocks = new HashMap<>();

    public RegionSave(Region region) {
        region.getBlocks().forEach(block ->
                blocks.put(new BlockVector(new Location(block.getWorld(), block.getX(), block.getY(), block.getZ()).toVector()), block.getType()));
    }

    public BlockVector blockAlign(Vector vector) {
        return new BlockVector((int) vector.getX() + 0.5d, (int) vector.getY() + 0.5d, (int) vector.getZ() + 0.5d);
    }

    public Material getBlockAt(BlockVector loc) {
        return blocks.get(loc);
    }

    public void clear() {
        blocks.clear();
    }

}