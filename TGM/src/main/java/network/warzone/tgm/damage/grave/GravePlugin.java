package network.warzone.tgm.damage.grave;

import network.warzone.tgm.damage.grave.listener.PlayerListener;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

public class GravePlugin {
    private JavaPlugin plugin;

    @Getter private PlayerListener playerListener;

    public GravePlugin(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerListener = new PlayerListener(this);
        plugin.getServer().getPluginManager().registerEvents(playerListener, plugin);
    }


    public <T extends Event> T callEvent(T event) {
        plugin.getServer().getPluginManager().callEvent(event);
        return event;
    }
}
