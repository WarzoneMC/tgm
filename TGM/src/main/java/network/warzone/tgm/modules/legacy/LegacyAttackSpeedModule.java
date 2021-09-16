package network.warzone.tgm.modules.legacy;

import com.google.gson.JsonObject;
import network.warzone.tgm.TGM;
import network.warzone.tgm.config.TGMConfigReloadEvent;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Removes the swing delay added in Minecraft 1.9.
 */
public class LegacyAttackSpeedModule extends MatchModule implements Listener {
    private static boolean globalEnabled;

    private boolean mapOverride;

    static {
        loadConfig();
    }

    public static void loadConfig() {
        ConfigurationSection legacyConfig = TGM.get().getConfig().getConfigurationSection("legacy");
        globalEnabled = legacyConfig != null && legacyConfig.getBoolean("attack-speed");
    }

    @EventHandler
    public void onConfigReload(TGMConfigReloadEvent event) {
        loadConfig();
    }

    @Override
    public void load(Match match) {
        mapOverride = globalEnabled;

        JsonObject matchConfig = match.getMapContainer().getMapInfo().getJsonObject();
        if (!matchConfig.has("legacy")) return;

        JsonObject matchLegacyConfig = matchConfig.get("legacy").getAsJsonObject();
        if (matchLegacyConfig.has("attack-speed")) mapOverride = matchLegacyConfig.get("attack-speed").getAsBoolean();
    }

    public boolean isEnabled() {
        return mapOverride;
    }
}
