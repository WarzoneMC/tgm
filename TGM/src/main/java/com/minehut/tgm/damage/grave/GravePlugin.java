package com.minehut.tgm.damage.grave;

import com.minehut.tgm.damage.grave.listener.PlayerListener;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

public class GravePlugin {
    private JavaPlugin plugin;

    public GravePlugin(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new PlayerListener(this), plugin);
    }


    public <T extends Event> T callEvent(T event) {
        plugin.getServer().getPluginManager().callEvent(event);
        return event;
    }
}
