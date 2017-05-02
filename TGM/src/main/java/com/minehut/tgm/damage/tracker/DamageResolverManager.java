package com.minehut.tgm.damage.tracker;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

/**
 * Manages the available {@link DamageResolver}s and provides a convenience
 * method to resolve damage using a list of resolvers.
 *
 * It is important what order resolvers are called in since a resolver called
 * after another that returns a non-null damage will override the previous
 * damage. Order can be specified when registering by indicating the types of
 * resolvers that should be called first. Note that there is no guarantee of
 * the order outside of what is specified.
 */
public interface DamageResolverManager {
    /**
     * Indicates whether the specified resolver is already registered.
     *
     * @param resolver Specified resolver
     * @return true if the resolver is registered, false otherwise
     *
     * @throws NullPointerException if resolver is null
     */
    boolean isRegistered(@Nonnull DamageResolver resolver);

    /**
     * Registers the specified resolver.
     *
     * @param resolver Specified resolver
     *
     * @throws NullPointerException if resolver is null
     */
    void register(@Nonnull DamageResolver resolver);

    /**
     * Registers the specified resolver indicating that certain resolvers must
     * be called before the specified one.
     *
     * @param resolver Specified resolver
     * @param before Classes of resolvers that must be called before this one
     *
     * @throws NullPointerException if resolver or before is null
     * @throws IllegalArgumentException if the constraints specified by the
     *                                  before classes cannot be solved
     */
    void register(@Nonnull DamageResolver resolver, @Nonnull Class<? extends DamageResolver>... before);

    /**
     * Registers the specified resolver indication that certain resolvers must
     * be called before the specified one.
     *
     * @param resolver Specified resolver
     * @param before Classes of resolvers that must be called before this one
     *
     * @throws NullPointerException if resolver or before is null
     * @throws IllegalArgumentException if the constraints specified by the
     *                                  before classes cannot be solved
     */
    void register(@Nonnull DamageResolver resolver, @Nonnull Collection<Class<? extends DamageResolver>> before);

    /**
     * Unregisters the specified resolver.
     *
     * @param resolver Specified resolver
     *
     * @throws NullPointerException if resolver is null
     */
    void unregister(@Nonnull DamageResolver resolver);

    /**
     * Gets the set of resolvers in the order
     *
     * @return Set of resolvers in the order they should be called
     */
    @Nonnull
    Set<DamageResolver> getResolvers();

    /**
     * Resolves the specified damage event using the set of registered
     * resolvers.
     *
     * If no resolver can resolve the specified event, this method will return
     * a generic {@link BukkitDamageInfo} instance.
     *
     * @param entity Entity that is being damaged
     * @param lifetime Lifetime of the entity up to this point
     * @param damageEvent Bukkit damage event to resolve
     * @return Resolved damage information (never null)
     *
     * @throws NullPointerException if entity, lifetime, or the event is null
     */
    @Nonnull
    DamageInfo resolve(@Nonnull LivingEntity entity, @Nonnull Lifetime lifetime, @Nonnull EntityDamageEvent damageEvent);
}
