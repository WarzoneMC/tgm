package network.warzone.tgm.modules.legacy;

import com.google.gson.JsonObject;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class LegacyShieldModule extends MatchModule implements Listener {

    private static final boolean globalEnabled;
    private static double projectileReductionFactor;
    private static double genericReductionFactor;

    private boolean mapOverride;

    static {
        ConfigurationSection legacyConfig = TGM.get().getConfig().getConfigurationSection("legacy");
        globalEnabled = legacyConfig != null && legacyConfig.getBoolean("shield");

        if (legacyConfig != null && legacyConfig.isConfigurationSection("shield-reduction")) {
            ConfigurationSection shieldConfig = legacyConfig.getConfigurationSection("shield-reduction");
            projectileReductionFactor = shieldConfig.getDouble("projectile");
            genericReductionFactor = shieldConfig.getDouble("generic");
        }
    }

    @Override
    public void load(Match match) {
        JsonObject matchConfig = match.getMapContainer().getMapInfo().getJsonObject();
        if (!matchConfig.has("legacy")) return;

        JsonObject matchLegacyConfig = matchConfig.get("legacy").getAsJsonObject();
        if (matchLegacyConfig.has("shield")) mapOverride = matchLegacyConfig.get("shield").getAsBoolean();
        else mapOverride = globalEnabled;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player)) return;

        if (!(event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) < 0)) return;

        double damage = event.getDamage();

        if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
            event.setDamage(EntityDamageEvent.DamageModifier.BLOCKING, -damage * projectileReductionFactor);
        } else {
            event.setDamage(EntityDamageEvent.DamageModifier.BLOCKING, -damage * genericReductionFactor);
        }
    }

    private boolean isEnabled() {
        return mapOverride;
    }

}
