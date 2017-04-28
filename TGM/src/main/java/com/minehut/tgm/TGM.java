package com.minehut.tgm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.minehut.tgm.map.MapInfo;
import com.minehut.tgm.map.MapInfoDeserializer;
import com.minehut.tgm.match.MatchManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by luke on 4/27/17.\
 *
 * Team Game Manager
 */


public class TGM extends JavaPlugin {
    @Getter public static TGM tgm;
    @Getter private Gson gson;
    @Getter private MatchManager matchManager;

    @Override
    public void onEnable() {
        tgm = this;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(MapInfo.class, new MapInfoDeserializer());
        this.gson = gsonBuilder.create();

        matchManager = new MatchManager(getConfig());
    }

    public static MatchManager getMatchManager() {
        return getTgm().getMatchManager();
    }
}
