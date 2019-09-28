package network.warzone.tgm.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolVersion;

/**
 * Created by Jorge on 09/28/2019
 */
public class Plugins {

    private static final String PROTOCOL_SUPPORT = "ProtocolSupport";

    public static boolean isProtocolSupportPresent() {
        return isPresent(PROTOCOL_SUPPORT);
    }

    public static boolean isPresent(String name) {
        return Bukkit.getPluginManager().isPluginEnabled(name);
    }

    public static class ProtocolSupport {

        public static boolean isUsingOldVersion(Player player) {
            return ProtocolSupportAPI.getProtocolVersion(player).isBefore(ProtocolVersion.MINECRAFT_1_9);
        }

    }

}
