package com.minehut.tgm.damage.tracker.trackers;

import com.minehut.tgm.damage.tracker.Tracker;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface AnvilTracker extends Tracker {
    boolean hasOwner(@Nonnull FallingBlock entity);

    @Nullable
    OfflinePlayer getOwner(@Nonnull FallingBlock anvil);

    @Nullable
    OfflinePlayer setOwner(@Nonnull FallingBlock anvil, @Nullable OfflinePlayer offlinePlayer);

    boolean hasPlacer(@Nonnull Block block);

    @Nullable
    OfflinePlayer getPlacer(@Nonnull Block block);

    @Nullable
    OfflinePlayer setPlacer(@Nonnull Block block, @Nonnull OfflinePlayer offlinePlayer);

    @Nonnull
    OfflinePlayer clearPlacer(@Nullable Block block);
}
