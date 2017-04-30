package com.minehut.tgm.playerList;

import com.minehut.tgm.user.PlayerContext;
import com.mojang.authlib.properties.Property;

public interface PlayerListController {
    void refreshView(PlayerContext playerContext);

    Property getBlankTexture();
}
