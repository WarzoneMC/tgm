package network.warzone.tgm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import network.warzone.tgm.command.PrivateMessageCommands;
import network.warzone.tgm.command.PunishmentCommands;
import network.warzone.warzoneapi.client.TeamClient;
import network.warzone.warzoneapi.client.http.HttpClient;
import network.warzone.warzoneapi.client.http.HttpClientConfig;
import network.warzone.warzoneapi.client.offline.OfflineClient;
import network.warzone.tgm.api.ApiManager;
import network.warzone.tgm.command.CycleCommands;
import network.warzone.tgm.damage.grave.GravePlugin;
import network.warzone.tgm.damage.tracker.plugin.TrackerPlugin;
import network.warzone.tgm.join.JoinManager;
import network.warzone.tgm.map.MapInfo;
import network.warzone.tgm.map.MapInfoDeserializer;
import network.warzone.tgm.match.MatchManager;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.GameRuleModule;
import network.warzone.tgm.player.PlayerManager;
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
        commandManager.register(PunishmentCommands.class); // Made by michael / Thrasilias
        commandManager.register(PrivateMessageCommands.class); // Made by michael / Thrasilias

        GameRuleModule.setGameRules(Bukkit.getWorlds().get(0)); //Set gamerules in main unused world

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
