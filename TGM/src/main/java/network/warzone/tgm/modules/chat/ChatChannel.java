package network.warzone.tgm.modules.chat;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
@NoArgsConstructor
public enum ChatChannel {
        ALL, TEAM, STAFF("tgm.staffchat");

        private String permission;

        public boolean hasPermission(Player player) {
            return permission == null || player.hasPermission(permission);
        }

        public static ChatChannel byName(String name) {
            for (ChatChannel channel : values()) {
                if (channel.name().equalsIgnoreCase(name)) {
                    return channel;
                }
            }
            return null;
        }
}
