package network.warzone.tgm.damage.tracker.base;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import network.warzone.tgm.damage.tracker.Tracker;
import org.bukkit.Bukkit;
import org.bukkit.World;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Abstract class for implementing {@link Tracker}.
 *
 * Implements the stateful operations of enabling / disabling the tracker in
 * certain worlds and notifies the child class through the {@link #onEnable}
 * and {@link #onDisable} methods.
 */
public abstract class AbstractTracker implements Tracker {
    /**
     * Method called when the tracker is enabled for a world.
     *
     * @param world World the tracker is now enabled in
     */
    protected void onEnable(World world) { }

    /**
     * Method called when the tracker is disabled for a world.
     *
     * Will be called before the {@link #clear} method.
     *
     * @param world World the tracker is now disabled in
     */
    protected void onDisable(World world) { }

    // implementation
    private boolean enabled = false;
    private final Set<World> oppositeWorlds = Sets.newHashSet();

    public boolean isEnabled(@Nonnull World world) {
        Preconditions.checkNotNull(world, "world");

        boolean opposite = this.oppositeWorlds.contains(world);

        return this.enabled ^ opposite;
    }

    public boolean enable() {
        return this.setEnabled(true);
    }

    public boolean enable(@Nonnull World world) {
        return this.setEnabled(true, world);
    }

    public boolean disable() {
        return this.setEnabled(false);
    }

    public boolean disable(@Nonnull World world) {
        return this.setEnabled(false, world);
    }

    private boolean setEnabled(boolean now) {
        Set<World> changed = this.getOppositeWorlds(now);

        this.enabled = now;
        this.oppositeWorlds.clear();

        for(World world : changed) {
            this.notifyChange(now, world);
        }

        return !changed.isEmpty();
    }

    private boolean setEnabled(boolean now, @Nonnull World world) {
        Preconditions.checkNotNull(world, "world");

        boolean changed;

        if(this.enabled == now) {
            changed = this.oppositeWorlds.remove(world);
        } else {
            changed = this.oppositeWorlds.add(world);
        }

        if(changed) {
            this.notifyChange(now, world);
        }

        return changed;
    }

    private Set<World> getOppositeWorlds(boolean now) {
        if(this.enabled == now) {
            return ImmutableSet.copyOf(this.oppositeWorlds);
        } else {
            Set<World> opp = Sets.newHashSet(Bukkit.getWorlds());
            opp.removeAll(this.oppositeWorlds);
            return ImmutableSet.copyOf(opp);
        }
    }

    private void notifyChange(boolean now, @Nonnull World world) {
        if(now) {
            this.onDisable(world);
            this.clear(world);
        } else {
            this.onEnable(world);
        }
    }
}
