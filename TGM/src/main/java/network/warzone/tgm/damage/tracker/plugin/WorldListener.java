package network.warzone.tgm.damage.tracker.plugin;

import com.google.common.base.Preconditions;
import network.warzone.tgm.damage.tracker.Tracker;
import network.warzone.tgm.damage.tracker.TrackerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;

import javax.annotation.Nonnull;

public class WorldListener implements Listener {
    private final @Nonnull
    TrackerManager manager;

    public WorldListener(@Nonnull TrackerManager manager) {
        Preconditions.checkNotNull(manager, "tracker manager");

        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(final WorldUnloadEvent event) {
        for(Tracker tracker : this.manager.getTrackers()) {
            tracker.clear(event.getWorld());
        }
    }
}
