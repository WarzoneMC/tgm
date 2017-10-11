package network.warzone.tgm.damage.tracker;

import network.warzone.tgm.damage.tracker.base.SimpleLifetimeManager;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Lifetimes {
    private Lifetimes() { }

    public static @Nonnull
    LifetimeManager getManager() {
        if (manager == null) {
            manager = new SimpleLifetimeManager();
        }
        return manager;
    }

    public static @Nonnull
    Lifetime getLifetime(@Nonnull LivingEntity entity) {
        return getManager().getLifetime(entity);
    }

    private static @Nullable
    LifetimeManager manager;
}
