package network.warzone.tgm.modules.kit;

import network.warzone.tgm.modules.team.MatchTeam;
import org.bukkit.entity.Player;

public interface KitNode {
    void apply(Player player, MatchTeam matchTeam);
}
