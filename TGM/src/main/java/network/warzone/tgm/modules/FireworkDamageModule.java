package network.warzone.tgm.modules;

import network.warzone.tgm.match.MatchModule;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class FireworkDamageModule extends MatchModule implements Listener {

    @EventHandler // Call before knockback.
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework) {
            event.setCancelled(true);
        }
    }
}
