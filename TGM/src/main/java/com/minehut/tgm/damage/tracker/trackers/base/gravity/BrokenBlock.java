package com.minehut.tgm.damage.tracker.trackers.base.gravity;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class BrokenBlock {
    private static final float PLAYER_WIDTH = 0.6f;
    private static final float PLAYER_RADIUS = PLAYER_WIDTH / 2.0f;

    final public Block block;
    final public Player breaker;
    final public long time;

    public BrokenBlock(Block block, Player breaker, long time) {
        this.block = block;
        this.breaker = breaker;
        this.time = time;
    }

    public static BrokenBlock lastBlockBrokenUnderPlayer(Player player, HashMap<Location, BrokenBlock> blocks) {
        Location location = player.getLocation();

        int y = (int) Math.floor(location.getY() - 0.1);

        int x1 = (int) Math.floor(location.getX() - PLAYER_RADIUS);
        int z1 = (int) Math.floor(location.getZ() - PLAYER_RADIUS);

        int x2 = (int) Math.floor(location.getX() + PLAYER_RADIUS);
        int z2 = (int) Math.floor(location.getZ() + PLAYER_RADIUS);

        BrokenBlock lastBrokenBlock = null;

        for(int x = x1; x <= x2; ++x) {
            for(int z = z1; z <= z2; ++z) {
                Location bl = new Location(location.getWorld(), x, y, z);

                if(blocks.containsKey(bl)) {
                    BrokenBlock brokenBlock = blocks.get(bl);
                    if(lastBrokenBlock == null || brokenBlock.time > lastBrokenBlock.time) {
                        lastBrokenBlock = brokenBlock;
                    }
                }
            }
        }

        return lastBrokenBlock;
    }
}
