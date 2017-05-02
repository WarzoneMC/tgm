package com.minehut.tgm.damage.tracker.plugin;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.minehut.tgm.damage.tracker.DamageInfo;
import com.minehut.tgm.damage.tracker.event.EntityDamageEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Helper class for the {@link DamageAPI} and Tracker plugin to properly handle
 * damage inflicted through the API.
 *
 * Class is intended for internal use only. Methods are not guaranteed to
 * respect versioning contract.
 */
public final class DamageAPIHelper {
    /**
     * Gets the singleton DamageAPIHelper instance.
     *
     * @return DamageAPIHelper instance (never null)
     */
    public static @Nonnull
    DamageAPIHelper get() {
        return inst;
    }
    private static final @Nonnull
    DamageAPIHelper inst = new DamageAPIHelper();

    /**
     * Fetches the given {@link DamageInfo} object specified by
     * {@link DamageAPI#inflictDamage} so it can be fetched by the
     * {@link DamageAPIResolver} when plugins who listen to the Bukkit event
     * ask for it.
     *
     * @param event EntityDamageEvent to fetch the given damage for
     * @return Stored damage info or null if none is stored
     *
     * @throws NullPointerException if event is null
     */
    public @Nullable
    DamageInfo getEventDamageInfo(@Nonnull org.bukkit.event.entity.EntityDamageEvent event) {
        Preconditions.checkNotNull(event, "entity damage event");
        return this.eventDamageMapping.get(event);
    }

    /**
     * Sets the API damage info that corresponds to the specified event.
     *
     * Intended for internal use only, so this method is not guaranteed to stay
     * the same through stable version releases.
     *
     * @param event Specified event
     * @param info Damage info that describes the damage or null to clear the
     *               store information
     * @return Previous damage info associated with the event
     *
     * @throws NullPointerException if event is null
     */
    public @Nullable
    DamageInfo setEventDamageInfo(@Nonnull org.bukkit.event.entity.EntityDamageEvent event, @Nullable DamageInfo info) {
        Preconditions.checkNotNull(event, "entity damage event");

        if(info != null) {
            return this.eventDamageMapping.put(event, info);
        } else {
            return this.eventDamageMapping.remove(event);
        }
    }

    private final @Nonnull
    Map<org.bukkit.event.entity.EntityDamageEvent, DamageInfo> eventDamageMapping = Maps.newHashMap();

    /**
     * Gets our version of the EntityDamageEvent so it can be called in
     * parallel to Bukkit's.
     *
     * @param bukkit Bukkit's version of the EntityDamageEvent
     * @return Our version of the event or null if there is none registered
     *
     * @throws NullPointerException if bukkit is null
     */
    public @Nullable
    EntityDamageEvent getOurEvent(@Nonnull org.bukkit.event.entity.EntityDamageEvent bukkit) {
        Preconditions.checkNotNull(bukkit, "entity damage event");

        return this.eventMapping.get(bukkit);
    }

    /**
     * Sets our version of the EntityDamageEvent so it can be called in
     * parallel with Bukkit's.
     *
     * @param bukkit Bukkit's version of the EntityDamageEvent
     * @param our Our version of the EntityDamageEvent or null to clear it
     * @return The previous association or null if none existed
     *
     * @throws NullPointerException if bukkit is null
     */
    public @Nullable
    EntityDamageEvent setOurEvent(@Nonnull org.bukkit.event.entity.EntityDamageEvent bukkit, @Nullable EntityDamageEvent our) {
        Preconditions.checkNotNull(bukkit, "entity damage event");

        if(our != null) {
            return this.eventMapping.put(bukkit, our);
        } else {
            return this.eventMapping.remove(bukkit);
        }
    }

    private final @Nonnull
    Map<org.bukkit.event.entity.EntityDamageEvent, EntityDamageEvent> eventMapping = Maps.newHashMap();
}
