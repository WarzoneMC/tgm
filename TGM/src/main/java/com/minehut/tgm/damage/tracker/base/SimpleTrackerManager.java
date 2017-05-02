package com.minehut.tgm.damage.tracker.base;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.minehut.tgm.damage.tracker.Tracker;
import com.minehut.tgm.damage.tracker.TrackerManager;
import org.bukkit.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class SimpleTrackerManager implements TrackerManager {
    public Set<Tracker> getTrackers() {
        return ImmutableSet.copyOf(this.trackers.values());
    }

    public boolean hasTracker(@Nonnull Class<? extends Tracker> trackerClass) {
        return this.getTracker(trackerClass) != null;
    }

    public @Nullable
    <T extends Tracker> T getTracker(@Nonnull Class<T> trackerClass) {
        Preconditions.checkNotNull(trackerClass, "tracker class");

        return findTracker(this.trackers, trackerClass);
    }

    public @Nullable
    <T extends Tracker> T setTracker(@Nonnull Class<T> trackerClass, @Nullable T tracker) {
        Preconditions.checkNotNull(trackerClass, "tracker class");
        Preconditions.checkArgument(trackerClass.isInstance(tracker), "tracker is not an instance of the specified class");

        return setTrackerInDB(this.trackers, trackerClass, tracker);
    }

    @SuppressWarnings("unchecked")
    public @Nullable
    <T extends Tracker> T clearTracker(@Nonnull Class<T> trackerClass) {
        Preconditions.checkNotNull(trackerClass, "tracker class");

        return (T) this.trackers.remove(trackerClass);
    }

    @SuppressWarnings("unchecked")
    public @Nullable
    <T extends Tracker> T clearTracker(@Nonnull Class<T> trackerClass, @Nonnull Class<? extends T> trackerImplClass) {
        Preconditions.checkNotNull(trackerClass, "tracker class");
        Preconditions.checkNotNull(trackerImplClass, "tracker implementation class");

        Tracker tracker = this.trackers.get(trackerClass);
        if(trackerImplClass.isInstance(tracker)) {
            return (T) this.trackers.remove(trackerClass);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static @Nullable
    <T extends Tracker> T findTracker(@Nonnull Map<Class<? extends Tracker>, Tracker> db, @Nonnull Class<T> search) {
        for(Map.Entry<Class<? extends Tracker>, Tracker> entry : db.entrySet()) {
            if(search.isAssignableFrom(entry.getKey())) {
                return (T) entry.getValue();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static @Nullable
    <T extends Tracker> T setTrackerInDB(@Nonnull Map<Class<? extends Tracker>, Tracker> db, @Nonnull Class<T> trackerClass, @Nullable T tracker) {
        if(tracker != null) {
            return (T) db.put(trackerClass, tracker);
        } else {
            return (T) db.remove(trackerClass);
        }
    }

    public void clearTrackers(@Nonnull World world) {
        Preconditions.checkNotNull(world, "world");

        for(Tracker tracker : this.getTrackers()) {
            tracker.clear(world);
        }
    }

    private final Map<Class<? extends Tracker>, Tracker> trackers = Maps.newHashMap();
}
