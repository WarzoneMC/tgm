package network.warzone.tgm.damage.tracker.base;

import com.google.common.base.Preconditions;
import network.warzone.tgm.damage.tracker.Lifetime;
import network.warzone.tgm.damage.tracker.LifetimeManager;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.WeakHashMap;

public class SimpleLifetimeManager implements LifetimeManager {
    public @Nonnull
    Lifetime getLifetime(@Nonnull LivingEntity entity) {
        Preconditions.checkNotNull(entity, "entity");

        Lifetime lifetime = this.lifetimes.computeIfAbsent(entity, k -> new SimpleLifetime());

        return lifetime;
    }

    public @Nonnull
    Lifetime setLifetime(@Nonnull LivingEntity entity, @Nonnull Lifetime lifetime) {
        Preconditions.checkNotNull(entity, "entity");
        Preconditions.checkNotNull(lifetime, "lifetime");

        Lifetime old = this.lifetimes.put(entity, lifetime);
        if(old != null) {
            return old;
        } else {
            return new SimpleLifetime();
        }
    }

    public @Nonnull
    Lifetime newLifetime(@Nonnull LivingEntity entity) {
        Preconditions.checkNotNull(entity, "entity");

        Lifetime lifetime = new SimpleLifetime();
        this.lifetimes.put(entity, lifetime);

        return lifetime;
    }

    public @Nonnull
    Lifetime endLifetime(@Nonnull LivingEntity entity) {
        Preconditions.checkNotNull(entity, "entity");

        Lifetime old = this.lifetimes.remove(entity);
        if(old != null) {
            return old;
        } else {
            return new SimpleLifetime();
        }
    }

    private final Map<LivingEntity, Lifetime> lifetimes = new WeakHashMap<>();
}
