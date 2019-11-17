package network.warzone.tgm.modules.team;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.user.PlayerContext;

@AllArgsConstructor
public class TeamJoinControllerImpl implements TeamJoinController {
    @Getter private TeamManagerModule teamManagerModule;

    @Override
    public MatchTeam determineTeam(PlayerContext playerContext) {
        return teamManagerModule.getSpectators();
    }
}
