package com.minehut.teamapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.List;

@AllArgsConstructor
public class Heartbeat {
    @Getter private final String name;
    @Getter private final String id;
    @Getter private List<String> players; //object ids
    @Getter private int playerCount;
    @Getter private int spectatorCount;
    @Getter private int maxPlayers;
    @Getter private String map;
    @Getter private String gametype;
}
