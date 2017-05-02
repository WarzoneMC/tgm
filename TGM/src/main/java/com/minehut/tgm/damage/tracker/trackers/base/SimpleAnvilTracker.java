package com.minehut.tgm.damage.tracker.trackers.base;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.minehut.tgm.damage.tracker.base.AbstractTracker;
import com.minehut.tgm.damage.tracker.trackers.AnvilTracker;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SimpleAnvilTracker extends AbstractTracker implements AnvilTracker {
    HashMap<Block, OfflinePlayer> placedAnvils = Maps.newHashMap();
    HashMap<FallingBlock, OfflinePlayer> ownedAnvils = Maps.newHashMap();

    public boolean hasOwner(@Nonnull FallingBlock anvil) {
        Preconditions.checkNotNull(anvil, "anvil");

        return this.ownedAnvils.containsKey(anvil);
    }

    @Nullable
    public OfflinePlayer getOwner(@Nonnull FallingBlock anvil) {
        Preconditions.checkNotNull(anvil, "anvil");

        return this.ownedAnvils.get(anvil);
    }

    @Nullable
    public OfflinePlayer setOwner(@Nonnull FallingBlock anvil, @Nullable OfflinePlayer offlinePlayer) {
        Preconditions.checkNotNull(anvil, "anvil");

        if(offlinePlayer != null) {
            return this.ownedAnvils.put(anvil, offlinePlayer);
        } else {
            return this.ownedAnvils.remove(anvil);
        }
    }

    public boolean hasPlacer(@Nonnull Block block) {
        Preconditions.checkNotNull(block, "block");

        return this.placedAnvils.containsKey(block);
    }

    @Nullable
    public OfflinePlayer getPlacer(@Nonnull Block block) {
        Preconditions.checkNotNull(block, "block");

        return this.placedAnvils.get(block);
    }

    @Nullable
    public OfflinePlayer setPlacer(@Nonnull Block block, @Nonnull OfflinePlayer offlinePlayer) {
        Preconditions.checkNotNull(block, "block");
        Preconditions.checkNotNull(offlinePlayer, "player");

        return this.placedAnvils.put(block, offlinePlayer);
    }

    @Nonnull
    public OfflinePlayer clearPlacer(@Nullable Block block) {
        Preconditions.checkNotNull(block, "block");

        return this.placedAnvils.remove(block);
    }

    public void clear(@Nonnull World world) {
        // clear information about blocks in that world
        Iterator<Map.Entry<Block, OfflinePlayer>> blockIt = this.placedAnvils.entrySet().iterator();
        while(blockIt.hasNext()) {
            Block block = blockIt.next().getKey();
            if(block.getWorld().equals(world)) {
                blockIt.remove();
            }
        }

        // clear information about entitys in that world
        Iterator<Map.Entry<FallingBlock, OfflinePlayer>> entityIt = this.ownedAnvils.entrySet().iterator();
        while(entityIt.hasNext()) {
            Entity tnt = entityIt.next().getKey();
            if(tnt.getWorld().equals(world)) {
                entityIt.remove();
            }
        }
    }

}
