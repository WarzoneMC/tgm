package com.minehut.tgm.modules;

import com.minehut.tgm.TGM;
import com.minehut.tgm.match.MatchModule;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by MatrixTunnel on 8/30/2017.
 *
 * https://github.com/gvlfm78/BukkitOldCombatMechanics/blob/master/src/main/java/gvlfm78/plugin/OldCombatMechanics/module/ModulePlayerRegen.java
 * @author gvlfm78 & Rayzr522
 */
public class RegenModule extends MatchModule implements Listener {

    private Map<UUID, Long> healTimes = new HashMap<>();

    @EventHandler
    public void onRegen(EntityRegainHealthEvent event) {
        if (event.getEntityType() != EntityType.PLAYER || event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) return;
        event.setCancelled(true);

        Player player = (Player) event.getEntity();
        long currentTime = System.currentTimeMillis() / 1000;

        if (currentTime - getLastHealTime(player) < 3)
            return;

        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        if (player.getHealth() < maxHealth) {
            player.setHealth(clamp(player.getHealth() + 1, 0.0, maxHealth));
            healTimes.put(player.getUniqueId(), currentTime);
        }

        float exhToApply = (float) 3;

        Bukkit.getScheduler().runTaskLater(TGM.get(), () -> {
            //This is because bukkit doesn't stop the exhaustion change when cancelling the event
            player.setExhaustion(player.getExhaustion() + exhToApply);
            //debug("Exhaustion before: " + previousExh + " Now: " + p.getExhaustion() + "Saturation: " + p.getSaturation(), p);
        }, 1L);
    }

    private long getLastHealTime(Player p) {
        if (!healTimes.containsKey(p.getUniqueId()))
            healTimes.put(p.getUniqueId(), System.currentTimeMillis() / 1000);

        return healTimes.get(p.getUniqueId());
    }

    /**
     * Clamps a value between a minimum and a maximum.
     *
     * @param value The value to clamp.
     * @param min   The minimum value to clamp to.
     * @param max   The maximum value to clamp to.
     * @return The clamped value.
     */
    public static double clamp(double value, double min, double max) {
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
