package com.minehut.tgm.map;

import com.avaje.ebean.text.json.JsonElement;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonParseException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
@AllArgsConstructor
public class TGMMap {
    @Getter private String name;
    @Getter private String version;
    @Getter private List<String> authors;
    @Getter private GameType gametype;
    @Getter private List<ParsedTeam> teams;
}
