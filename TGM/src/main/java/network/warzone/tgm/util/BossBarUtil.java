package network.warzone.tgm.util;

import org.bukkit.boss.BossBar;

/**
 * Created by Jorge on 09/28/2019
 */
public class BossBarUtil {

    public static void displayForOldVersions(BossBar bar) {
        if (!Plugins.isProtocolSupportPresent()) return;
        if (bar.isVisible()) {
            bar.getPlayers().stream().filter(Plugins.ProtocolSupport::isUsingOldVersion)
                    .forEach(player -> player.sendActionBar(bar.getTitle()));
        }
    }

}
