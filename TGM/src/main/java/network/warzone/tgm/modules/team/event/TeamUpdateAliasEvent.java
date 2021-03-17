package network.warzone.tgm.modules.team.event;

import lombok.Getter;
import network.warzone.tgm.modules.team.MatchTeam;

/**
 * Created by Jorge on 03/17/2021
 */
@Getter
public class TeamUpdateAliasEvent extends TeamUpdateEvent {

    private final String oldAlias;
    private final String newAlias;

    public TeamUpdateAliasEvent(MatchTeam matchTeam, String oldAlias, String newAlias) {
        super(matchTeam);
        this.oldAlias = oldAlias;
        this.newAlias = newAlias;
    }
}
