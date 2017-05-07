package com.minehut.tgm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.minehut.teamapi.client.TeamClient;
import com.minehut.teamapi.client.http.HttpClient;
import com.minehut.teamapi.client.http.HttpClientConfig;
import com.minehut.teamapi.client.offline.OfflineClient;
import com.minehut.tgm.api.ApiManager;
import com.minehut.tgm.command.CycleCommands;
import com.minehut.tgm.damage.grave.GravePlugin;
import com.minehut.tgm.damage.tracker.plugin.TrackerPlugin;
import com.minehut.tgm.join.JoinManager;
import com.minehut.tgm.map.MapInfo;
import com.minehut.tgm.map.MapInfoDeserializer;
import com.minehut.tgm.match.MatchManager;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.player.PlayerManager;
import com.minehut.tgm.playerList.PlayerListManager;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TGM extends JavaPlugin {
    public static TGM tgm;
    @Getter private Gson gson;
    @Getter private TeamClient teamClient;

    private CommandsManager<CommandSender> commands;
    private CommandsManagerRegistration commandManager;

    @Getter private MatchManager matchManager;
    @Getter private PlayerManager playerManager;
    @Getter private JoinManager joinManager;
    @Getter private TrackerPlugin tracker;
    @Getter private GravePlugin grave;
    @Getter private ApiManager apiManager;

    public static TGM get() {
        return tgm;
    }

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

        this.commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender sender, String perm) {
                return sender instanceof ConsoleCommandSender || sender.hasPermission(perm);
            }
        };

        matchManager = new MatchManager(fileConfiguration);
        playerManager = new PlayerManager();
        joinManager = new JoinManager();
//        playerListManager = new PlayerListManager();
        tracker = new TrackerPlugin(this);
        grave = new GravePlugin(this);
        apiManager = new ApiManager();

        this.commandManager = new CommandsManagerRegistration(this, this.commands);
        commandManager.register(CycleCommands.class);

        try {
            matchManager.cycleNextMatch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        try {
            this.commands.execute(commandLabel, args, sender, sender);
        } catch (CommandPermissionsException e) {
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.RED + "Insufficient permissions.");
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission.");
            }
        } catch (com.sk89q.minecraft.util.commands.CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandException e) {
            sender.sendMessage(e.getMessage());
        }
        return true;
    }

    public static void registerEvents(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, TGM.get());
    }

    @SuppressWarnings("unchecked")
    public <T extends MatchModule> T getModule(Class<T> clazz) {
        return matchManager.getMatch().getModule(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T extends MatchModule> List<T> getModules(Class<T> clazz) {
        return matchManager.getMatch().getModules(clazz);
    }
}
