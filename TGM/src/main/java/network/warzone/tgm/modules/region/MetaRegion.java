package network.warzone.tgm.modules.region;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.Getter;
import network.warzone.tgm.TGM;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorge on 10/09/2019
 */
@Getter
public class MetaRegion implements Region {

    private List<Region> regions = new ArrayList<>();

    private World world;
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;
    private Location min;
    private Location max;

    public MetaRegion(JsonArray jsonArray) {
        for (JsonElement element : jsonArray) {
            this.regions.add(TGM.get().getModule(RegionManagerModule.class).getRegion(TGM.get().getMatchManager().getMatch(), element));
        }
        this.world = TGM.get().getMatchManager().getMatch().getWorld();
        calculateMinMax();
    }

    @Override
    public boolean contains(Location location) {
        for (Region region : getRegions()) {
            if (region.contains(location)) return true;
        }
        return false;
    }

    @Override
    public boolean contains(Block block) {
        return contains(block.getLocation());
    }

    @Override
    public Location getCenter() {
        return new Location(world, (minX + maxX) / 2D, (minY + maxY) / 2D, (minZ + maxZ) / 2D);
    }

    @Override
    public List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<>();
        for (Region region : getRegions()) {
            blocks.addAll(region.getBlocks());
        }
        return blocks;
    }

    @Override
    public Location getMin() {
        return this.min;
    }

    @Override
    public Location getMax() {
        return this.max;
    }

    private void calculateMinMax() {
        minX = 0; minY = 0; minZ = 0;
        maxX = 0; maxY = 0; maxZ = 0;
        if (regions != null && !regions.isEmpty()) {
            Region r = regions.get(0);
            minX = r.getMin().getBlockX();
            minY = r.getMin().getBlockY();
            minZ = r.getMin().getBlockZ();

            maxX = r.getMax().getBlockX();
            maxY = r.getMax().getBlockY();
            maxZ = r.getMax().getBlockZ();
            for (Region region : getRegions()) {
                Block min = region.getMin().getBlock();
                Block max = region.getMax().getBlock();
                if (minX > min.getX()) minX = min.getX();
                if (minY > min.getY()) minY = min.getY();
                if (minZ > min.getZ()) minZ = min.getZ();

                if (maxX < max.getX()) maxX = max.getX();
                if (maxY < max.getY()) maxY = max.getY();
                if (maxZ < max.getZ()) maxZ = max.getZ();
            }
        }
        this.min = new Location(world, minX, minY, minZ);
        this.max = new Location(world, maxX, maxY, maxZ);
    }
}
