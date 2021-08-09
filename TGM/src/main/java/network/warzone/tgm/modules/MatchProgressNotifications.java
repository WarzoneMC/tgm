package network.warzone.tgm.modules;

import net.kyori.adventure.text.format.NamedTextColor;
import network.warzone.tgm.match.MatchModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import static net.kyori.adventure.text.Component.text;


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
        Bukkit.broadcast(text("The match has started!", NamedTextColor.GREEN));
    }

}
