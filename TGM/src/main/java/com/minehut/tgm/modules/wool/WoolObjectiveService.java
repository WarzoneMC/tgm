package com.minehut.tgm.modules.wool;

import com.minehut.tgm.modules.team.MatchTeam;
import org.bukkit.entity.Player;

public interface WoolObjectiveService {
    void pickup(Player player, MatchTeam matchTeam);

    void place(Player player, MatchTeam matchTeam);
}
