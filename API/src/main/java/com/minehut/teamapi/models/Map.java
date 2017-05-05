package com.minehut.teamapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public class Map {
    @Getter private String name;
    @Getter private String version;
    @Getter private List<String> authors;
    @Getter private String gametype;
    @Getter private List<Team> teams;
}
