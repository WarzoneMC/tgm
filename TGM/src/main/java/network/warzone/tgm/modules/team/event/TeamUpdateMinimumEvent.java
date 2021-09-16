package network.warzone.tgm.modules.team.event;

import lombok.Getter;
import network.warzone.tgm.modules.team.MatchTeam;

/**
 * Created by Jorge on 03/17/2021
 */
@Getter
public class TeamUpdateMinimumEvent extends TeamUpdateEvent {

    private final int oldMinimum;
    private final int newMinimum;

    public TeamUpdateMinimumEvent(MatchTeam matchTeam, int oldMinimum, int newMinimum) {
        super(matchTeam);
        this.oldMinimum = oldMinimum;
        this.newMinimum = newMinimum;
    }
}
