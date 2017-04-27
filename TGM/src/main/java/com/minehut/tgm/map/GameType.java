package com.minehut.tgm.map;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by luke on 4/27/17.
 */
@AllArgsConstructor
public enum GameType {
    TDM("TDM"),
    CUSTOM("Custom");

    @Getter private String name;
}
