package com.minehut.tgm.join;

import com.minehut.teamapi.models.UserProfile;
import com.minehut.tgm.user.PlayerContext;
import org.bukkit.entity.Player;

/**
 * Created by luke on 4/27/17.
 */
public interface LoginService {
    public void login(PlayerContext playerContext);
}
