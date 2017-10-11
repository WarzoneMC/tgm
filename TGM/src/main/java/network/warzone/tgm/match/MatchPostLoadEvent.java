package network.warzone.tgm.match;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called after all modules have loaded.
 */

@AllArgsConstructor
public class MatchPostLoadEvent extends Event {
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
