package network.warzone.tgm.modules.tdm;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Jorge on 9/7/2017.
 */

@AllArgsConstructor @Getter
public enum TDMObjective {

    /**
     * Kills will count towards the killer's team score.
    */
    KILLS("Kills"),
    /**
     * Deaths will count towards the opponent team scores.
     * Useful for maps where the deaths are not necessarily caused by the other team(s).
     */
    DEATHS("Deaths");

    private final String name;
}
