package network.warzone.tgm.damage.tracker;

import network.warzone.tgm.damage.tracker.base.SimpleResolverManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class DamageResolvers {
    private DamageResolvers() { }

    public static @Nonnull
    DamageResolverManager getManager() {
        if (manager == null) {
            manager = new SimpleResolverManager();
        }
        return manager;
    }

    private static @Nullable
    DamageResolverManager manager = null;
}
