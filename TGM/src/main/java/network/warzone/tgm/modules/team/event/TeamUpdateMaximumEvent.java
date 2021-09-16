package network.warzone.tgm.modules.team.event;

import lombok.Getter;
import network.warzone.tgm.modules.team.MatchTeam;

/**
 * Created by Jorge on 03/17/2021
 */
@Getter
public class TeamUpdateMaximumEvent extends TeamUpdateEvent {

    private final int oldMaximum;
    private final int newMaximum;

    public TeamUpdateMaximumEvent(MatchTeam matchTeam, int oldMaximum, int newMaximum) {
        super(matchTeam);
        this.oldMaximum = oldMaximum;
        this.newMaximum = newMaximum;
    }
}
