package network.warzone.tgm.util;

import java.util.Collection;

import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import lombok.AllArgsConstructor;
import net.minecraft.server.v1_16_R1.ChatMessageType;
import net.minecraft.server.v1_16_R1.IChatBaseComponent;
import net.minecraft.server.v1_16_R1.PacketPlayOutChat;
import net.minecraft.server.v1_16_R1.SystemUtils;
import protocolsupport.api.ProtocolVersion;

public class ChatUtil {
    public static void sendChatComponents(Collection<Player> players, IChatBaseComponent[] components) {
        for (Player player : players) sendChatComponents(player, components);
    }

    public static void sendChatComponents(Player player, IChatBaseComponent[] components) {
        CraftPlayer obcPlayer = (CraftPlayer) player;
        for (IChatBaseComponent component : components) {
            obcPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutChat(
                component,
                ChatMessageType.CHAT,
                SystemUtils.b
            ));
        }
    }

    @AllArgsConstructor
    public static class ColorFallbackComponent {
        public final String message;
        public final String fallbackMessage;

        public String getMessageForPlayer(Player player) {
            return Plugins.ProtocolSupport.usingVersionOrNewer(player, ProtocolVersion.MINECRAFT_1_16) ? this.message : this.fallbackMessage;
        }
    }
}
