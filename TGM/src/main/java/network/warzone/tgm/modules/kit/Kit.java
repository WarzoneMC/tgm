package network.warzone.tgm.modules.kit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.modules.team.MatchTeam;
import org.bukkit.entity.Player;

import java.util.List;

@AllArgsConstructor @Getter
public class Kit {

    private final String name;
    private final String description;
    private final List<KitNode> nodes;

    public void apply(Player player, MatchTeam matchTeam) {
        for (KitNode kitNode : nodes) {
            kitNode.apply(player, matchTeam);
        }
    }
}
