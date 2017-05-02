package com.minehut.tgm.damage.tracker.trackers.base;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.minehut.tgm.damage.tracker.base.AbstractTracker;
import com.minehut.tgm.damage.tracker.trackers.OwnedMobTracker;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Map.Entry;

public class SimpleOwnedMobTracker extends AbstractTracker implements OwnedMobTracker {
    private final Map<LivingEntity, Player> ownedMobs = Maps.newHashMap();

    public boolean hasOwner(@Nonnull LivingEntity entity) {
        Preconditions.checkNotNull(entity, "entity");

        return this.ownedMobs.containsKey(entity);
    }

    public @Nullable
    Player getOwner(@Nonnull LivingEntity entity) {
        Preconditions.checkNotNull(entity, "entity");

        return this.ownedMobs.get(entity);
    }

    public @Nonnull
    Player setOwner(@Nonnull LivingEntity entity, @Nonnull Player player) {
        Preconditions.checkNotNull(entity, "entity");
        Preconditions.checkNotNull(player, "player");

        return this.ownedMobs.put(entity, player);
    }

    public @Nonnull
    Player clearOwner(@Nonnull LivingEntity entity) {
        Preconditions.checkNotNull(entity, "entity");

        return this.ownedMobs.remove(entity);
    }

    public void clear(@Nonnull World world) {
        Preconditions.checkNotNull(world, "world");

        // clear information about owned mobs in that world
        for (Entry<LivingEntity, Player> livingEntityPlayerEntry : this.ownedMobs.entrySet()) {
            LivingEntity entity = livingEntityPlayerEntry.getKey();
            if (entity.getWorld().equals(world)) {
                entity.remove();
            }
        }
    }
}
