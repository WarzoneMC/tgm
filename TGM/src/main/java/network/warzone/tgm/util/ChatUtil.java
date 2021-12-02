package network.warzone.tgm.util;

import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;

public class ChatUtil {
    public static void sendChatComponents(Collection<Player> players, Component[] components) {
        for (Player player : players) sendChatComponents(player, components);
    }

    public static void sendChatComponents(Player player, Component[] components) {
        CraftPlayer obcPlayer = (CraftPlayer) player;
        for (Component component : components) {
            obcPlayer.getHandle().connection.connection.send(new ClientboundChatPacket(
                component,
                ChatType.CHAT,
                Util.NIL_UUID
            ));
        }
    }

}
