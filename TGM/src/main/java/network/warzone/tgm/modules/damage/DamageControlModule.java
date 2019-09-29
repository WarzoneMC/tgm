package network.warzone.tgm.modules.damage;

import com.google.gson.JsonObject;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DamageControlModule extends MatchModule implements Listener {
    private boolean fireDamage = true;
    private boolean fallDamage = true;
    private boolean suffocationDamage = true;

    @Override
    public void load(Match match) {
        JsonObject matchObj = match.getMapContainer().getMapInfo().getJsonObject();
        if (matchObj.has("damageControl")) {
            JsonObject damageControlObj = matchObj.getAsJsonObject("damageControl");
            if (damageControlObj.has("fire")) fireDamage = damageControlObj.get("fire").getAsBoolean();
            if (damageControlObj.has("fall")) fallDamage = damageControlObj.get("fall").getAsBoolean();
            if (damageControlObj.has("suffocation")) suffocationDamage = damageControlObj.get("suffocation").getAsBoolean();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageEvent event) {
        boolean shouldCancel = false;
        if((event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK) && !fireDamage) shouldCancel = true;
        else if(event.getCause() == DamageCause.SUFFOCATION && !suffocationDamage) shouldCancel = true;
        else if(event.getCause() == DamageCause.FALL && !fallDamage) shouldCancel = true;
        if (shouldCancel) event.setCancelled(true);
    }
}
