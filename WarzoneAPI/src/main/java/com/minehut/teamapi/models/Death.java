package com.minehut.teamapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Death {
    @Getter private String player; //id
    @Getter private String killer; //id

    @Getter private String playerItem;
    @Getter private String killerItem;

    @Getter private String map; //id
    @Getter private String match; //id
}
