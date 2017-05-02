package com.minehut.tgm.damage.tracker;

import org.bukkit.World;

import javax.annotation.Nonnull;

/**
 * Common interface that all services should implement in order to track data
 * in worlds.
 *
 * It is important to control what data is tracked on a world basis, keeping in
 * mind that worlds may be loaded and unloaded at any time.
 */
public interface Tracker {
    /**
     * Indicates whether the tracker is enabled and collecting data in the
     * specified world.
     *
     * @param world Specified world
     * @return true if the tracker is enabled in the world, false otherwise
     *
     * @throws NullPointerException if world is null
     */
    boolean isEnabled(@Nonnull World world);

    /**
     * Enables the tracker in all current and future worlds.
     *
     * @return true if one or more worlds that were not enabled are now
     *         enabled, false otherwise
     */
    boolean enable();

    /**
     * Enables the tracker in the specified world.
     *
     * @param world Specified world
     * @return true if the world was disabled, false otherwise
     *
     * @throws NullPointerException if world is null
     */
    boolean enable(@Nonnull World world);

    /**
     * Disables the tracker in all current and future worlds.
     *
     * Note that any data that was tracked will be discarded.
     *
     * @return true if one or more worlds that were enabled are now disabled,
     *         false otherwise
     */
    boolean disable();

    /**
     * Disables the tracker in the specified world.
     *
     * Note that any data that was tracked will be discarded.
     *
     * @param world Specified world
     * @return true if the world was enabled, false otherwise
     *
     * @throws NullPointerException if world is null
     */
    boolean disable(@Nonnull World world);

    /**
     * Clears stored information regarding the specified world.
     *
     * @param world Specified world
     *
     * @throws NullPointerException if world is null
     */
    void clear(@Nonnull World world);
}
