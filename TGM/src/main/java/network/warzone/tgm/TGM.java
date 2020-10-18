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
import network.warzone.tgm.command.*;
import network.warzone.tgm.join.JoinManager;
import network.warzone.tgm.map.MapInfo;
import network.warzone.tgm.map.MapInfoDeserializer;
import network.warzone.tgm.map.Rotation;
import network.warzone.tgm.map.RotationDeserializer;
import network.warzone.tgm.match.MatchManager;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.GameRuleModule;
import network.warzone.tgm.modules.killstreak.Killstreak;
import network.warzone.tgm.modules.killstreak.KillstreakDeserializer;
import network.warzone.tgm.nickname.NickManager;
import network.warzone.tgm.parser.effect.EffectDeserializer;
import network.warzone.tgm.parser.item.ItemDeserializer;
import network.warzone.tgm.player.PlayerManager;
import network.warzone.tgm.util.Plugins;
import network.warzone.tgm.util.menu.PunishMenu;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Getter
public class TGM extends JavaPlugin {

    public static TGM instance;

    @Getter
    private Properties gitInfo = new Properties();

    private Gson gson;
    private TeamClient teamClient;

    private MatchManager matchManager;
    private PlayerManager playerManager;
    private JoinManager joinManager;
    private ApiManager apiManager;
    private NickManager nickManager;

    private BroadcastManager broadcastManager;

    private CommandsManager<CommandSender> commands;
    private CommandsManagerRegistration commandManager;

    @Getter private long startupTime;

    public static TGM get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        this.startupTime = new Date().getTime();
        FileConfiguration fileConfiguration = getConfig();
        saveDefaultConfig();

        gson = new GsonBuilder()
                // TGM
                .registerTypeAdapter(MapInfo.class, new MapInfoDeserializer())
                .registerTypeAdapter(Killstreak.class, new KillstreakDeserializer())
                .registerTypeAdapter(Rotation.class, new RotationDeserializer())
                // Bukkit
                .registerTypeAdapter(ItemStack.class, new ItemDeserializer())
                .registerTypeAdapter(PotionEffect.class, new EffectDeserializer())

                .create();

        ConfigurationSection apiConfig = fileConfiguration.getConfigurationSection("api");
        if (apiConfig != null && apiConfig.getBoolean("enabled")) {
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
        matchManager.getMapRotation().refresh();

        playerManager = new PlayerManager();
        joinManager = new JoinManager();
        apiManager = new ApiManager();
        broadcastManager = new BroadcastManager();

        this.commandManager = new CommandsManagerRegistration(this, this.commands);

        commandManager.register(CycleCommands.class);
        commandManager.register(BroadcastCommands.class);
        commandManager.register(MiscCommands.class);
        commandManager.register(NickCommands.class);
        commandManager.register(RotationCommands.class);
        if (apiConfig.getBoolean("enabled", false)) {
            commandManager.register(PunishCommands.class);
            commandManager.register(TagCommands.class);
            commandManager.register(RankCommands.class);
        }

        PunishMenu.getPresetsMenu().load();

        GameRuleModule.setGameRuleDefaults(Bukkit.getWorlds().get(0)); //Set gamerules in main unused world

        Plugins.checkSoftDependencies();

        matchManager.cycleNextMatch();
        nickManager = new NickManager(); 
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

    public <T extends MatchModule> T getModule(Class<T> clazz) {
        return matchManager.getMatch().getModule(clazz);
    }

    public <T extends MatchModule> List<T> getModules(Class<T> clazz) {
        return matchManager.getMatch().getModules(clazz);
    }

}
