package com.minehut.tgm.modules.tdm;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Jorge on 9/7/2017.
 */

@AllArgsConstructor
public enum TDMObjective {

    KILLS("Kills"),
    DEATHS("Deaths");

    @Getter String name;

}
