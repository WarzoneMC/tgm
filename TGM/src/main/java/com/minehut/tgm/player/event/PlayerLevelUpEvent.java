package com.minehut.tgm.player.event;

import com.minehut.tgm.user.PlayerContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Jorge on 10/7/2017.
 */

@AllArgsConstructor @Getter
public class PlayerLevelUpEvent extends Event {

    private PlayerContext playerContext;
    private int fromLevel;
    private int toLevel;

    protected static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
