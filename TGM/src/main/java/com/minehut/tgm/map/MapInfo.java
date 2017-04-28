package com.minehut.tgm.map;

import com.minehut.tgm.gametype.GameType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
@AllArgsConstructor
public class MapInfo {
    @Getter private String name;
    @Getter private String version;
    @Getter private List<String> authors;
    @Getter private GameType gametype;
    @Getter private List<ParsedTeam> teams;
}
