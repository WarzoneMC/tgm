package network.warzone.tgm.damage.tracker.trackers;

import network.warzone.tgm.damage.tracker.Tracker;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface DispenserTracker extends Tracker {
    boolean hasOwner(@Nonnull Entity entity);

    @Nullable
    OfflinePlayer getOwner(@Nonnull Entity entity);

    @Nullable
    OfflinePlayer setOwner(@Nonnull Entity entity, @Nullable Player player);

    boolean hasPlacer(@Nonnull Block block);

    @Nullable
    OfflinePlayer getPlacer(@Nonnull Block block);

    @Nullable
    OfflinePlayer setPlacer(@Nonnull Block block, @Nullable Player player);

    @Nonnull
    OfflinePlayer clearPlacer(@Nonnull Block block);
}
