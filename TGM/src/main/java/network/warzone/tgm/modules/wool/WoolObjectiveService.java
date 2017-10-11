package network.warzone.tgm.modules.wool;

import network.warzone.tgm.modules.team.MatchTeam;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface WoolObjectiveService {
    void pickup(Player player, MatchTeam matchTeam, boolean firstTouch);

    void place(Player player, MatchTeam matchTeam, Block block);

    void drop(Player player, MatchTeam matchTeam, boolean broadcast);
}
