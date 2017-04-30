package com.minehut.tgm.modules.team;

import com.minehut.tgm.user.PlayerContext;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TeamJoinControllerImpl implements TeamJoinController {
    @Getter private TeamManagerModule teamManagerModule;

    @Override
    public MatchTeam determineTeam(PlayerContext playerContext) {
        return teamManagerModule.getSpectators();
    }
}
