package network.warzone.tgm.player.event;


import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.user.PlayerContext;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Jorge on 10/7/2017.
 */

@AllArgsConstructor @Getter
public class PlayerXPEvent extends Event {

    private PlayerContext playerContext;
    private int amount;
    private int fromXP;
    private int toXP;

    protected static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
