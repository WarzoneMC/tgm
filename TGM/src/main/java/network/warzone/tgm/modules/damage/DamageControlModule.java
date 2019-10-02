package network.warzone.tgm.modules.damage;

import com.google.gson.JsonObject;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DamageControlModule extends MatchModule implements Listener {

    private HashMap<DamageCause, Boolean> enabled = new HashMap<>();

    @Override
    public void load(Match match) {
        JsonObject matchObj = match.getMapContainer().getMapInfo().getJsonObject();
        if (matchObj.has("damageControl")) {
            JsonObject damageControlObj = matchObj.getAsJsonObject("damageControl");
            for (DamageCause cause : DamageCause.values()) {
                enabled.put(cause, !damageControlObj.has(cause.name()) || damageControlObj.get(cause.name()).getAsBoolean());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageEvent event) {
        if (!this.enabled.getOrDefault(event.getCause(), true))
            event.setCancelled(true);
    }
}
