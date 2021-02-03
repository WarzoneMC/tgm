package network.warzone.tgm.map;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.map.source.GitRemoteMapSource;
import network.warzone.tgm.util.AsyncHelper;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Created by luke on 4/27/17.
 */
@Getter
public class MapLibrary {

    private final List<MapContainer> maps = new ArrayList<>();
    private final List<GitRemoteMapSource> remotes = new ArrayList<>();
    private final List<File> sources = new ArrayList<>();
    private final MapLoader mapLoader;
    private final ReentrantLock updateRemoteLock = new ReentrantLock();

    public MapLibrary(FileConfiguration fileConfiguration, MapLoader mapLoader) {
        ConfigurationSection mapSection = fileConfiguration.getConfigurationSection("map");
        ConfigurationSection remotesSection = mapSection.getConfigurationSection("remotes");

        if (remotesSection != null) {
            for (String remoteEntry : remotesSection.getKeys(false)) {
                ConfigurationSection remoteSection = remotesSection.getConfigurationSection(remoteEntry);
                Set<String> remoteEntryKeys = remoteSection.getKeys(false);
                if (!remoteEntryKeys.contains("destination")) {
                    System.out.println("There should be a destination, but one wasn't specified for the remote '" + remoteEntry + "'");
                    continue;
                } else if (!remoteEntryKeys.contains("uri")) {
                    System.out.println("There should be a URI, but one wasn't specified for the remote '" + remoteEntry + "'");
                    continue;
                }

                final String destination = remoteSection.getString("destination");
                final String remoteURI = remoteSection.getString("uri");
                final String branch = remoteSection.getString("branch");

                File destinationDirectory = new File(destination);

                if (!destinationDirectory.exists()) {
                    destinationDirectory.mkdirs();
                }
                if (!destinationDirectory.isDirectory()) {
                    System.out.println("The destination was not a valid directory, skipping remote '" + remoteEntry + "'");
                    continue;
                }

                System.out.println("Registered remote '" + remoteEntry + "'");
                this.remotes.add(new GitRemoteMapSource(remoteEntry, destinationDirectory, remoteURI, branch));
            }
            boolean updateRemotes = mapSection.getBoolean("update-remotes-on-startup", false);
            if (updateRemotes) this.updateRemotes();
        }

        this.updateSources();

        this.mapLoader = mapLoader;
    }

    public void refreshMaps() {
        maps.clear();
        for (File source : sources) {
            List<MapContainer> loaded = mapLoader.loadMaps(source);
            Bukkit.getLogger().info("Found " + loaded.size() + " maps in source " + source);
            maps.addAll(loaded);
        }
    }

    public void updateRemotes() {
        this.updateRemote("ALL");
    }

    public void updateRemote(final String remote) {
        this.updateRemote(remote, null);
    }

    public GitRemoteMapSource getRemoteByName(final String name) {
        for (GitRemoteMapSource remoteMapSource : this.remotes) {
            if (remoteMapSource.getSourceName().equalsIgnoreCase(name)) return remoteMapSource;
        }
        return null;
    }

    public void updateRemote(final String remote, @Nullable CommandSender sender) {
        if (this.updateRemoteLock.isLocked()) {
            if (sender != null) {
                sender.sendMessage(ChatColor.RED + "Remotes are currently being updated already");
            }
            return;
        }

        boolean updateAll = "ALL".equalsIgnoreCase(remote);

        List<Runnable> tasks = new ArrayList<>();
        if (updateAll) {
            for (GitRemoteMapSource remoteMapSource : this.remotes) {
                tasks.add(remoteMapSource::refreshMaps);
            }
        } else {
            GitRemoteMapSource remoteMapSource = this.getRemoteByName(remote);
            if (remoteMapSource != null) tasks.add(remoteMapSource::refreshMaps);
        }

        if (tasks.size() == 0) return;

        final String[] updateMessages = {"Updated sources with remotes. Do " + ChatColor.GREEN + "/loadmaps" + ChatColor.RESET + " to update maps with updated sources"};
        Runnable callback = () -> {
            this.updateRemoteLock.unlock();
            for (String updateMessage : updateMessages) Bukkit.getLogger().info(updateMessage);
        };
        if (sender != null) {
            callback = () -> {
                this.updateRemoteLock.unlock();
                sender.sendMessage(updateMessages);
            };
        }

        AsyncHelper.taskQueueWithLock(tasks, this.updateRemoteLock, callback);
    }

    public void updateSources() {
        this.updateSources(TGM.get().getConfig().getConfigurationSection("map").getStringList("sources"));
    }

    public void updateSources(Collection<String> dirSources) {
        for (String s : dirSources) {
            File sourceDirectory = new File(s);

            if (!sources.contains(sourceDirectory)) {
                sources.add(sourceDirectory);
                Bukkit.getLogger().info("Added map source: " + s);
            }
        }
    }
}
