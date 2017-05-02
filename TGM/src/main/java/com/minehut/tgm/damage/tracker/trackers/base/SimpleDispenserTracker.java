package com.minehut.tgm.damage.tracker.trackers.base;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.minehut.tgm.damage.tracker.base.AbstractTracker;
import com.minehut.tgm.damage.tracker.trackers.DispenserTracker;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SimpleDispenserTracker extends AbstractTracker implements DispenserTracker {
    private final HashMap<Block, OfflinePlayer> placedDispensers = Maps.newHashMap();
    private final HashMap<Entity, OfflinePlayer> ownedEntitys = Maps.newHashMap();

    public boolean hasOwner(@Nonnull Entity entity) {
        Preconditions.checkNotNull(entity, "entity");

        return this.ownedEntitys.containsKey(entity);
    }

    public @Nullable
    OfflinePlayer getOwner(@Nonnull Entity entity) {
        Preconditions.checkNotNull(entity, "entity");

        return this.ownedEntitys.get(entity);
    }

    public @Nullable
    OfflinePlayer setOwner(@Nonnull Entity entity, @Nullable Player player) {
        Preconditions.checkNotNull(entity, "tnt entity");

        if(player != null) {
            return this.ownedEntitys.put(entity, player);
        } else {
            return this.ownedEntitys.remove(entity);
        }
    }

    public boolean hasPlacer(@Nonnull Block block) {
        Preconditions.checkNotNull(block, "block");

        return this.placedDispensers.containsKey(block);
    }

    public @Nullable
    OfflinePlayer getPlacer(@Nonnull Block block) {
        Preconditions.checkNotNull(block, "block");

        return this.placedDispensers.get(block);
    }

    public @Nullable
    OfflinePlayer setPlacer(@Nonnull Block block, @Nonnull Player player) {
        Preconditions.checkNotNull(block, "block");
        Preconditions.checkNotNull(player, "player");

        return this.placedDispensers.put(block, player.getPlayer());
    }

    public @Nonnull
    OfflinePlayer clearPlacer(@Nonnull Block block) {
        Preconditions.checkNotNull(block, "block");

        return this.placedDispensers.remove(block);
    }

    public void clear(@Nonnull World world) {
        // clear information about blocks in that world
        Iterator<Map.Entry<Block, OfflinePlayer>> blockIt = this.placedDispensers.entrySet().iterator();
        while(blockIt.hasNext()) {
            Block block = blockIt.next().getKey();
            if(block.getWorld().equals(world)) {
                blockIt.remove();
            }
        }

        // clear information about entitys in that world
        Iterator<Map.Entry<Entity, OfflinePlayer>> entityIt = this.ownedEntitys.entrySet().iterator();
        while(entityIt.hasNext()) {
            Entity tnt = entityIt.next().getKey();
            if(tnt.getWorld().equals(world)) {
                entityIt.remove();
            }
        }
    }
}
