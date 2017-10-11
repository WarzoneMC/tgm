package network.warzone.tgm.damage.tracker.plugin;

import network.warzone.tgm.damage.tracker.Lifetimes;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class LifetimeListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        Lifetimes.getManager().newLifetime(event.getPlayer());
    }
}
