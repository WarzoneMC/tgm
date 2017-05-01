package com.minehut.tgm.modules.region;

import com.minehut.tgm.TGM;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RegionSave {
    private final HashMap<BlockVector, Pair<Material,Byte>> blocks;

    public RegionSave(Region region) {
        blocks = new HashMap<>();
        for (Block block : region.getBlocks()) {
            blocks.put(new BlockVector(new Location(block.getWorld(), block.getX(), block.getY(), block.getZ()).toVector()), new ImmutablePair<>(block.getType(), block.getData()));
        }
    }

    public BlockVector blockAlign(Vector vector) {
        return new BlockVector((int) vector.getX() + 0.5d, (int) vector.getY() + 0.5d, (int) vector.getZ() + 0.5d);
    }

    public Pair<Material,Byte> getBlockAt(BlockVector loc) {
        return blocks.get(loc);
    }

}