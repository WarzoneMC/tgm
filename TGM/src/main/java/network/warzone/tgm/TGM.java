package network.warzone.tgm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.Unirest;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.api.ApiManager;
import network.warzone.tgm.broadcast.BroadcastManager;
import network.warzone.tgm.command.BroadcastCommands;
import network.warzone.tgm.command.CycleCommands;
import network.warzone.tgm.command.PunishCommands;
import network.warzone.tgm.command.RankCommands;
import network.warzone.tgm.join.JoinManager;
import network.warzone.tgm.map.MapInfo;
import network.warzone.tgm.map.MapInfoDeserializer;
import network.warzone.tgm.match.MatchManager;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.GameRuleModule;
import network.warzone.tgm.player.PlayerManager;
import network.warzone.warzoneapi.client.TeamClient;
import network.warzone.warzoneapi.client.http.HttpClient;
import network.warzone.warzoneapi.client.http.HttpClientConfig;
import network.warzone.warzoneapi.client.offline.OfflineClient;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;

@Getter
public class TGM extends JavaPlugin {

    public static TGM instance;

    private Gson gson;
    private TeamClient teamClient;

    private MatchManager matchManager;
    private PlayerManager playerManager;
    private JoinManager joinManager;
    private ApiManager apiManager;

    private BroadcastManager broadcastManager;

    private CommandsManager<CommandSender> commands;
    private CommandsManagerRegistration commandManager;

    public static TGM get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        FileConfiguration fileConfiguration = getConfig();
        saveDefaultConfig();

        gson = new GsonBuilder().registerTypeAdapter(MapInfo.class, new MapInfoDeserializer()).create();

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

        commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender sender, String perm) {
                return sender.isOp() || sender.hasPermission(perm);
            }
        };

        matchManager = new MatchManager(fileConfiguration);
        playerManager = new PlayerManager();
        joinManager = new JoinManager();
        apiManager = new ApiManager();
        broadcastManager = new BroadcastManager();

        this.commandManager = new CommandsManagerRegistration(this, this.commands);

        commandManager.register(CycleCommands.class);
        commandManager.register(BroadcastCommands.class);
        commandManager.register(TeleportCommands.class);
        if (apiConfig.getBoolean("enabled", false)) {
            commandManager.register(PunishCommands.class);
            commandManager.register(RankCommands.class);
        }

        GameRuleModule.setGameRules(Bukkit.getWorlds().get(0)); //Set gamerules in main unused world

        matchManager.cycleNextMatch();
    }

    @Override
    public void onDisable() {


        try {
            Unirest.shutdown();
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

    public static void unregisterEvents(Listener listener) {
        HandlerList.unregisterAll(listener);
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
