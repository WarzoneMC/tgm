package com.minehut.teamapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Kill {
    @Getter private String player; //id
    @Getter private String target; //id

    @Getter private String playerItem;
    @Getter private String targetItem;

    @Getter private String map; //name
}
