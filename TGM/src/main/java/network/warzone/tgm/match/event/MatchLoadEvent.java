package network.warzone.tgm.match.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.match.Match;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player enters a match.
 *
 * This happens in two cases:
 * 1. A player joins the server.
 * 2. The map cycles.
 */

@AllArgsConstructor
public class MatchLoadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter private Match match;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
