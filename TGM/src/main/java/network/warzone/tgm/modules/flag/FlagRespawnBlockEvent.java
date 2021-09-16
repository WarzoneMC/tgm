package network.warzone.tgm.modules.flag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.modules.team.MatchTeam;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a Flag modifies a team's ability to respawn.
 */

@AllArgsConstructor @Getter
public class FlagRespawnBlockEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private MatchTeam affectedTeam;
    private boolean canRespawn;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
