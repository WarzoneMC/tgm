package network.warzone.tgm.broadcast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.Getter;
import network.warzone.tgm.TGM;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by Jorge on 4/14/2018.
 */
public class BroadcastManager {

    private FileConfiguration config;
    private File configFile = new File(TGM.get().getDataFolder().getAbsolutePath() + "/broadcasts.yml");

    private File broadcastsFile = new File(TGM.get().getDataFolder().getAbsolutePath() + "/broadcasts.json");

    @Getter private boolean autoBroadcast = true;
    @Getter private List<String> queue = new ArrayList<>();
    @Getter private int interval = 60*20; // ticks (20 ticks = 1 second)
    @Getter private int index = 0;
    private Map<String, List<String>> onEvents = new HashMap<>();

    @Getter private BukkitTask task;

    @Getter private String url;

    @Getter private List<Broadcast> broadcasts = new ArrayList<>();

    public BroadcastManager() {
        reload();
        new BroadcastEventTrigger(this);
    }

    private void retrieveBroadcasts() {
        if (this.url != null) {
            Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                try {
                    readFromURL();
                    startAutoBroadcast(false);
                } catch (UnirestException e) {
                    System.out.println("Could not retrieve broadcasts. Using local file.");
                    e.printStackTrace();
                    readFromFile();
                    startAutoBroadcast(false);
                }
            });
        }
        else {
            readFromFile();
            startAutoBroadcast(false);
        }
    }

    public void reload() {
        stopTask();
        loadConfig();
        retrieveBroadcasts();
    }

    private void readFromURL() throws UnirestException {
        HttpResponse<JsonNode> broadcasts = Unirest.get(url).asObject(JsonNode.class);
        Gson gson = new Gson();
        if (this.broadcasts != null && !this.broadcasts.isEmpty()) this.broadcasts.clear();
        this.broadcasts = new ArrayList(Arrays.asList(gson.fromJson(broadcasts.getBody().toString(), Broadcast[].class)));
    }

    private void readFromFile() {
        if (!this.broadcastsFile.exists()) TGM.get().saveResource("broadcasts.json", true);
        try {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(broadcastsFile));
            if (this.broadcasts != null && !this.broadcasts.isEmpty()) this.broadcasts.clear();
            this.broadcasts = new ArrayList(gson.fromJson(reader, new TypeToken<List<Broadcast>>() {}.getType()));
        } catch (FileNotFoundException e) {
            System.out.println("Could not load broadcasts file.");
            e.printStackTrace();
        }
    }

    public void startAutoBroadcast(boolean force) {
        if (isAutoBroadcast() || force) {
            startTask(this.interval);
        }
    }

    private void startTask(int interval) {
        task = Bukkit.getScheduler().runTaskTimer(TGM.get(), () -> {
            if (index >= queue.size()) index = 0;
            if (broadcasts.isEmpty() || queue.isEmpty()) return;
            Broadcast broadcast = getBroadcast(queue.get(index));
            if (broadcast != null) {
                broadcast(broadcast);
            }
            index++;
            if (index >= queue.size()) index = 0;

        }, interval, interval);
    }

    public boolean stopTask() {
        if (task != null) {
            Bukkit.getScheduler().cancelTask(task.getTaskId());
            task = null;
            return true;
        }
        return false;
    }

    public boolean setInterval(int interval) {
        if (!set("interval", interval)) return false;
        this.interval = interval;
        if (stopTask()) startAutoBroadcast(true);
        return true;
    }

    public boolean setAutobroadcast(boolean b) {
        if (!set("autobroadcast", b)) return false;
        this.autoBroadcast = b;
        if (stopTask()) startAutoBroadcast(true);
        return true;
    }

    public boolean setQueue(List<String> queue) {
        if (!set("queue", queue)) return false;
        this.queue = queue;
        if (stopTask()) startAutoBroadcast(true);
        return true;
    }

    public boolean setURL(String url) {
        if (!set("url", url)) return false;
        this.url = url;
        reload();
        return true;
    }

    private boolean set(String key, Object value) {
        if (this.config == null && !createConfig()) return false;
        this.config.set(key, value);
        try {
            this.config.save(configFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean createConfig() {
        configFile = new File(TGM.get().getDataFolder().getAbsolutePath() + "/broadcasts.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        return true;
    }

    private void loadConfig() {
        if (!configFile.exists()) return;

        this.config = YamlConfiguration.loadConfiguration(configFile);
        if (this.config.contains("autobroadcast") && this.config.isBoolean("autobroadcast")) this.autoBroadcast = this.config.getBoolean("autobroadcast");
        if (this.config.contains("queue") && this.config.isList("queue")) this.queue = this.config.getStringList("queue");
        if (this.config.contains("interval") && this.config.isInt("interval")) this.interval = this.config.getInt("interval");
        if (this.config.contains("url") && this.config.isString("url") && isURL(this.config.getString("url"))) this.url = this.config.getString("url");
        if (this.config.contains("events") && this.config.isConfigurationSection("events")) {
            onEvents.clear();
            ConfigurationSection events = this.config.getConfigurationSection("events");
            for (String key : events.getKeys(false)) {
                if (events.isList(key)) addOnEvent(key, events.getStringList(key));
                else addOnEvent(key, Collections.singletonList(events.getString(key)));
            }
        }
    }

    private boolean isURL(String urlS) {
        try {
            new URL(urlS);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private void addOnEvent(String name, List<String> ids) {
        for (String id : ids) {
            if (onEvents.containsKey(name)) {
                onEvents.get(name).add(id);
            } else {
                onEvents.put(name, ids);
                return;
            }
        }
    }

    protected List<Broadcast> getOnEvents(String name) {
        List<Broadcast> broadcasts = new ArrayList<>();
        for (String id : onEvents.getOrDefault(name, Collections.emptyList())) {
            Broadcast broadcast = getBroadcast(id);
            if (broadcast == null) continue;
            broadcasts.add(broadcast);
        }
        return broadcasts;
    }

    private Broadcast getBroadcast(String id) {
        for (Broadcast broadcast : this.broadcasts) {
            if (broadcast.getId().equals(id)) return broadcast;
        }
        return null;
    }

    public void broadcastRaw(String message) {
        Bukkit.broadcastMessage(format(message));
    }

    public void broadcastRaw(Player player, String message) {
        player.sendMessage(format(message));
    }

    public boolean broadcast(String id, String... args) {
        Broadcast broadcast;
        return !broadcasts.isEmpty() && (broadcast = getBroadcast(id)) != null && broadcast(broadcast, args);
    }

    public boolean broadcast(Player player, String id, String... args) {
        Broadcast broadcast;
        return !broadcasts.isEmpty() && (broadcast = getBroadcast(id)) != null && broadcast(player, broadcast, args);
    }

    public boolean broadcast(Broadcast broadcast, String... args) {
        String permission = broadcast.getPermission();
        if (permission != null) {
            if (permission.startsWith("!")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission(permission.substring(1, permission.length())))
                        broadcastRaw(player, String.format(broadcast.getMessage(), args));
                }
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission(permission)) broadcastRaw(player, String.format(broadcast.getMessage(), args));
                }
            }
        } else broadcastRaw(String.format(broadcast.getMessage(), args));
        return true;
    }

    public boolean broadcast(Player player, Broadcast broadcast, String... args) {
        String permission = broadcast.getPermission();
        if (permission != null) {
            if (permission.startsWith("!")) {
                if (!player.hasPermission(permission.substring(1, permission.length()))) broadcastRaw(player, String.format(broadcast.getMessage(), args));
            } else {
                if (player.hasPermission(permission)) broadcastRaw(player, String.format(broadcast.getMessage(), args));
            }
        } else broadcastRaw(player, broadcast.getMessage());
        return true;
    }

    private String format(String string) {
        return ChatColor.translateAlternateColorCodes('&', string.replace("\\n", "\n"));
    }

}
