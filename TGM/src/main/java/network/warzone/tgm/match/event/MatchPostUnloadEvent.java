package network.warzone.tgm.match.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.match.Match;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called after all modules have unloaded.
 */

@AllArgsConstructor
public class MatchPostUnloadEvent extends Event {
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
