package com.minehut.tgm.tab;

import com.minehut.tgm.user.PlayerContext;
import com.mojang.authlib.properties.Property;
import org.bukkit.entity.Player;

public interface PlayerListController {
    void refreshView(PlayerContext playerContext);

    Property getBlankTexture();
}
