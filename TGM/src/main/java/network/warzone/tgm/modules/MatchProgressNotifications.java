package network.warzone.tgm.modules;

import network.warzone.tgm.match.MatchModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;


/**
 * This is a stub class for debugging only. This should
 * not make it into release.
 *
 * Start/stop notifications should be broadcasted from
 * the countdown modules once they are implemented.
 *
 * This module could exist as one that periodically broadcasts
 * updates about how long a match has been going on for.
 *
 * Example: "5 minutes remaining."
 */

public class MatchProgressNotifications extends MatchModule {

    @Override
    public void enable() {
        Bukkit.broadcastMessage(ChatColor.GREEN + "The match has started!");
    }

}
