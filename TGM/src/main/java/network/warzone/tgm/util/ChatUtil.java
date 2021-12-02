package network.warzone.tgm.util;

import net.minecraft.SystemUtils;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;

public class ChatUtil {
    public static void sendChatComponents(Collection<Player> players, IChatBaseComponent[] components) {
        for (Player player : players) sendChatComponents(player, components);
    }

    public static void sendChatComponents(Player player, IChatBaseComponent[] components) {
        CraftPlayer obcPlayer = (CraftPlayer) player;
        for (IChatBaseComponent component : components) {
            obcPlayer.getHandle().b.a.a(new PacketPlayOutChat(
                component,
                ChatMessageType.a,
                SystemUtils.b
            ));
        }
    }

}
