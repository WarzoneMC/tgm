package network.warzone.tgm.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.boss.BossBar;

/**
 * Created by Jorge on 09/28/2019
 */
public class BossBarUtil {

    public static void displayForOldVersions(BossBar bar) {
        if (!Plugins.isProtocolSupportPresent()) return;
        if (bar.isVisible()) {
            bar.getPlayers().stream().filter(Plugins.ProtocolSupport::isUsingOldVersion)
                    .forEach(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(bar.getTitle())));
        }
    }

}
