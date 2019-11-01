package network.warzone.tgm.modules;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.MatchModule;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by MatrixTunnel on 8/30/2017.
 *
 * https://github.com/kernitus/BukkitOldCombatMechanics/blob/master/src/main/java/kernitus/plugin/OldCombatMechanics/module/ModulePlayerRegen.java
 * @author kernitus
 */
public class RegenModule extends MatchModule implements Listener {

    private Map<UUID, Long> healTimes = new HashMap<>();

    private int frequency = 3; // How often, in seconds, a player should regenerate health
    private int amount = 1; // How many half-hearts the player should heal by, every seconds specified above
    private int exhaustion = 1; // How much exhaustion healing should give to the player

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRegen(EntityRegainHealthEvent event) {
        if (event.getEntityType() != EntityType.PLAYER || event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) return;
        Player player = (Player) event.getEntity();

        event.setCancelled(true);
        long currentTime = System.currentTimeMillis() / 1000;

        if (currentTime - getLastHealTime(player) < frequency)
            return;

        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        if (player.getHealth() < maxHealth) {
            player.setHealth(clamp(player.getHealth() + amount, 0.0, maxHealth));
            healTimes.put(player.getUniqueId(), currentTime);
        }

        final float previousExhaustion = player.getExhaustion();
        final float exhaustionToApply = (float) exhaustion;

        Bukkit.getScheduler().runTaskLater(TGM.get(), () -> {
            //This is because bukkit doesn't stop the exhaustion change when cancelling the event
            player.setExhaustion(previousExhaustion + exhaustionToApply);
        }, 1L);
    }

    @Override
    public void unload() {
        healTimes.clear();
    }

    private long getLastHealTime(Player player) {
        if (!healTimes.containsKey(player.getUniqueId()))
            healTimes.put(player.getUniqueId(), System.currentTimeMillis() / 1000);

        return healTimes.get(player.getUniqueId());
    }

    /**
     * Clamps a value between a minimum and a maximum.
     *
     * For all the math utilities that I needed which (for some reason) aren't in the Math class.
     * @author Rayzr
     *
     * @param value The value to clamp.
     * @param min   The minimum value to clamp to.
     * @param max   The maximum value to clamp to.
     * @return The clamped value.
     */
    private static double clamp(double value, double min, double max) {
        double realMin = Math.min(min, max);
        double realMax = Math.max(min, max);

        if (value < realMin) {
            value = realMin;
        }

        if (value > realMax) {
            value = realMax;
        }

        return value;
    }

}
