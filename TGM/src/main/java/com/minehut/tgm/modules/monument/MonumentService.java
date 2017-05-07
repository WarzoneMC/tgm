package com.minehut.tgm.modules.monument;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface MonumentService {
    void damage(Player player, Block block);

    void destroy(Player player, Block block);
}
