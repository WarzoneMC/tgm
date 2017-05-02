package com.minehut.tgm.damage.tracker;

import org.bukkit.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public interface TrackerManager {
    Set<Tracker> getTrackers();

    boolean hasTracker(@Nonnull Class<? extends Tracker> trackerClass);

    @Nullable
    <T extends Tracker> T getTracker(@Nonnull Class<T> trackerClass);

    @Nullable
    <T extends Tracker> T setTracker(@Nonnull Class<T> trackerClass, @Nullable T tracker);

    @Nullable
    <T extends Tracker> T clearTracker(@Nonnull Class<T> trackerClass);

    @Nullable
    <T extends Tracker> T clearTracker(@Nonnull Class<T> trackerClass, @Nonnull Class<? extends T> trackerImplClass);

    void clearTrackers(@Nonnull World world);
}
