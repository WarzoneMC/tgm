package com.minehut.tgm.damage.tracker.plugin;

import com.minehut.tgm.damage.tracker.event.EntityDamageEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DebugListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(final EntityDamageEvent event) {
        Bukkit.broadcastMessage(event.getEntity().toString() + " damaged for " + event.getDamage() + " raw half hearts at " + event.getLocation() + " info: " + event.getInfo() + " cancelled?" + (event.isCancelled() ? "yes" : "no"));
    }
}
