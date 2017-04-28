package com.minehut.tgm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.minehut.teamapi.client.TeamClient;
import com.minehut.teamapi.client.http.HttpClient;
import com.minehut.teamapi.client.http.HttpClientConfig;
import com.minehut.teamapi.client.offline.OfflineClient;
import com.minehut.tgm.join.JoinManager;
import com.minehut.tgm.map.MapInfo;
import com.minehut.tgm.map.MapInfoDeserializer;
import com.minehut.tgm.match.MatchManager;
import com.minehut.tgm.player.PlayerManager;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

/**
 * Created by luke on 4/27/17.\
 *
 * Team Game Manager
 */


public class TGM extends JavaPlugin {
    @Getter public static TGM tgm;
    @Getter private Gson gson;
    @Getter private TeamClient teamClient;

    @Getter private MatchManager matchManager;
    @Getter private PlayerManager playerManager;
    @Getter private JoinManager joinManager;

    @Override
    public void onEnable() {
        tgm = this;
        FileConfiguration fileConfiguration = getConfig();
        saveDefaultConfig();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(MapInfo.class, new MapInfoDeserializer());
        this.gson = gsonBuilder.create();



        ConfigurationSection apiConfig = fileConfiguration.getConfigurationSection("api");
        if (apiConfig.getBoolean("enabled")) {
            teamClient = new HttpClient(new HttpClientConfig() {
                @Override
                public String getBaseUrl() {
                    return apiConfig.getString("url");
                }

                @Override
                public String getAuthToken() {
                    return apiConfig.getString("auth");
                }
            });
        } else {
            teamClient = new OfflineClient();
        }

        matchManager = new MatchManager(fileConfiguration);
        playerManager = new PlayerManager();
        joinManager = new JoinManager();

        try {
            matchManager.cycleNextMatch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MatchManager getMatchManager() {
        return getTgm().getMatchManager();
    }
    public static PlayerManager getPlayerManager() { return getTgm().getPlayerManager(); }
}
