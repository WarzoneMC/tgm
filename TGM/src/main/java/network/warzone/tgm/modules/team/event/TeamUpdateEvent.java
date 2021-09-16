package network.warzone.tgm.modules.team.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.modules.team.MatchTeam;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a team property value is updated.
 *
 * Example: Team alias is changed.
 */

@AllArgsConstructor
public class TeamUpdateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter private MatchTeam matchTeam;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
