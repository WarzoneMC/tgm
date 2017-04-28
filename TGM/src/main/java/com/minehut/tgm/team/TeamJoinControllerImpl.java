package com.minehut.tgm.team;

import com.minehut.tgm.user.PlayerContext;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TeamJoinControllerImpl implements TeamJoinController {
    @Getter private TeamManager teamManager;

    @Override
    public MatchTeam determineTeam(PlayerContext playerContext) {
        return teamManager.getSpectators();
    }
}
