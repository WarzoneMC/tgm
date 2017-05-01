package com.minehut.tgm.modules.kit;

import com.minehut.tgm.modules.team.MatchTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.List;

@AllArgsConstructor
public class Kit {
    @Getter private final String name;
    @Getter private final String description;
    @Getter private final List<KitNode> nodes;

    public void apply(Player player, MatchTeam matchTeam) {
        for (KitNode kitNode : nodes) {
            kitNode.apply(player, matchTeam);
        }
    }
}
