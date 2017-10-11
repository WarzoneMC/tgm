package network.warzone.tgm.modules.controlpoint;

import network.warzone.tgm.modules.team.MatchTeam;

public interface ControlPointService {
    void holding(MatchTeam matchTeam);

    void capturing(MatchTeam matchTeam, int progress, int maxProgress, boolean upward);

    void captured(MatchTeam matchTeam);

    void lost(MatchTeam matchTeam);
}
