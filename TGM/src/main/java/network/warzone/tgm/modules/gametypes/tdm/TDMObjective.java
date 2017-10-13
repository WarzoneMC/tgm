package network.warzone.tgm.modules.gametypes.tdm;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Jorge on 9/7/2017.
 */
@AllArgsConstructor @Getter
public enum TDMObjective {

    KILLS("Kills"),
    DEATHS("Deaths");

    private String name;

}
