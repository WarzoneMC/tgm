package com.minehut.tgm.user;

import com.minehut.teamapi.models.UserProfile;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * Created by luke on 4/27/17.
 */
public class PlayerContext {
    @Getter private Player player;
    @Getter private UserProfile userProfile;

    public PlayerContext(Player player, UserProfile userProfile) {
        this.player = player;
        this.userProfile = userProfile;
    }
}
