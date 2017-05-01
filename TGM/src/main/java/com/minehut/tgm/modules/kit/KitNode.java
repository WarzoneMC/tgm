package com.minehut.tgm.modules.kit;

import com.minehut.tgm.modules.team.MatchTeam;
import org.bukkit.entity.Player;

public interface KitNode {
    void apply(Player player, MatchTeam matchTeam);
}
