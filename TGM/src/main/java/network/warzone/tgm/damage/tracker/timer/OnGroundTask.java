package network.warzone.tgm.damage.tracker.timer;

import network.warzone.tgm.damage.tracker.event.PlayerOnGroundEvent;
import network.warzone.tgm.damage.tracker.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OnGroundTask implements Runnable {
    private final Plugin plugin;
    private final Map<UUID, Boolean> grounded;
    private int taskId;

    public OnGroundTask(Plugin plugin) {
        this.plugin = plugin;
        this.grounded = new HashMap<>();
    }

    public void start() {
        this.taskId = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, this, 0, 3);
    }

    public void stop() {
        this.plugin.getServer().getScheduler().cancelTask(this.taskId);
    }

    @Override
    public void run() {
        Map<UUID, Boolean> clone = new HashMap<>(this.grounded);

        for (Player player : Bukkit.getOnlinePlayers()) {
            Boolean last = clone.get(player.getUniqueId());
            boolean current = player.isOnGround();

            if (last == null || last != current) {
                PlayerOnGroundEvent call = new PlayerOnGroundEvent(player, current);
                this.grounded.put(player.getUniqueId(), current);

                for (EventPriority priority : EventPriority.values())
                    EventUtil.callEvent(call, PlayerOnGroundEvent.getHandlerList(), priority);
            }
        }

        // Remove players who have logged out
        for (UUID uuid : clone.keySet()) {
            if (Bukkit.getPlayer(uuid) == null)
                this.grounded.remove(uuid);
        }
    }
}
