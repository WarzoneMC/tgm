package network.warzone.tgm.damage.tracker.trackers;

import network.warzone.tgm.damage.tracker.Tracker;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface OwnedMobTracker extends Tracker {
    boolean hasOwner(@Nonnull LivingEntity entity);

    @Nullable
    Player getOwner(@Nonnull LivingEntity entity);

    @Nullable
    Player setOwner(@Nonnull LivingEntity entity, @Nullable Player player);
}
