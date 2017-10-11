package network.warzone.tgm.damage.tracker;

import org.joda.time.Instant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.ListIterator;

/**
 * Represents the lifetime of an entity.
 *
 * Provides a convenient way to record damage objects pertaining to the victim.
 */
public interface Lifetime {
    @Nullable
    Instant getStart();

    void setStart(@Nonnull Instant start);

    @Nullable
    Instant getEnd();

    void setEnd(@Nonnull Instant end);

    @Nonnull
    List<Damage> getDamage();

    @Nonnull
    ListIterator<Damage> getDamageFirst();

    @Nonnull
    ListIterator<Damage> getDamageLast();

    @Nullable
    Damage getFirstDamage();

    @Nullable
    Damage getLastDamage();

    /**
     * Gets the last damage instance where the info object is an instance of
     * the specified class. Uses {@link Class#isInstance(Object)} to check info
     * objects.
     *
     * @param damageInfoClass DamageInfo class to check for
     * @return Last damage that matched or null if none matched
     *
     * @throws NullPointerException if damageInfoClass is null
     */
    @Nullable
    Damage getLastDamage(@Nonnull Class<? extends DamageInfo> damageInfoClass);

    void addDamage(@Nonnull Damage damage);
}
