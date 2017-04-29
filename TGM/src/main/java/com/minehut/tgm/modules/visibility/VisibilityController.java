package com.minehut.tgm.modules.visibility;

import org.bukkit.entity.Player;

public interface VisibilityController {
    boolean canSee(Player eyes, Player target);
}
