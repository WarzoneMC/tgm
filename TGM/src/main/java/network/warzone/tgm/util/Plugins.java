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
    public static boolean USING_PROTOCOL_SUPPORT = false;

    public static boolean isProtocolSupportPresent() {
        return isPresent(PROTOCOL_SUPPORT);
    }
    
    public static void checkProtocolSupport() {
        USING_PROTOCOL_SUPPORT = isProtocolSupportPresent();
    }

    public static boolean isPresent(String name) {
        return Bukkit.getPluginManager().isPluginEnabled(name);
    }

    public static class ProtocolSupport {

        public static boolean isUsingOldVersion(Player player) {
            ProtocolVersion version = ProtocolSupportAPI.getProtocolVersion(player);
            return version == ProtocolVersion.UNKNOWN || version.isBefore(ProtocolVersion.MINECRAFT_1_9);
        }

        public static boolean usingVersionOrNewer(Player player, ProtocolVersion version) {
            ProtocolVersion playerVersion = ProtocolSupportAPI.getProtocolVersion(player);
            return version != ProtocolVersion.UNKNOWN && !playerVersion.isBefore(version);
        }
    }

}
