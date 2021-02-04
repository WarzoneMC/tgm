package network.warzone.tgm.command;

import cl.bgmp.minecraft.util.commands.CommandContext;
import cl.bgmp.minecraft.util.commands.annotations.Command;
import cl.bgmp.minecraft.util.commands.annotations.CommandPermissions;
import cl.bgmp.minecraft.util.commands.annotations.NestedCommand;
import cl.bgmp.minecraft.util.commands.annotations.TabCompletion;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import network.warzone.tgm.TGM;
import network.warzone.tgm.config.TGMConfigReloadEvent;
import network.warzone.tgm.file.BytebinUploader;
import network.warzone.tgm.nickname.ProfileCache;
import network.warzone.tgm.util.ServerUtil;
import network.warzone.tgm.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by Jorge on 02/03/2021
 */

public class TGMCommand {

    private static BytebinUploader uploader;

    public static BytebinUploader getUploader() {
        if (uploader == null) {
            uploader = new BytebinUploader(getURLFromConfig());
            TGM.registerEvents(new Listener() {
                @EventHandler
                public void configReload(TGMConfigReloadEvent event) {
                    uploader.setUrl(getURLFromConfig());
                }
            });
        }
        return uploader;
    }

    private static String getURLFromConfig() {
        return TGM.get().getConfig().getString("heapdump.url", "http://localhost:8080");
    }

    @Command(aliases = "cleardumps", desc = "Delete all heap dumps")
    @CommandPermissions({"tgm.command.tgm.heapdump"})
    public static void clearheapdump(CommandContext context, CommandSender sender) {
        FileConfiguration config = TGM.get().getConfig();
        String dir = config.getString("heapdump.dir");
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
            try {
                File directory = new File(dir);
                if (!directory.isDirectory()) {
                    sender.sendMessage(ChatColor.RED + "File is not a directory.");
                    return;
                }
                for (File file : Objects.requireNonNull(directory.listFiles())) {
                    if (file.getName().endsWith(".phd") || file.getName().endsWith(".hprof")) {
                        try {
                            Files.deleteIfExists(file.toPath());
                        } catch (IOException e) {
                            sender.sendMessage(ChatColor.RED + "Failed to delete heap dump '" + file.getName() + "'. See sever log for details");
                            e.printStackTrace();
                        }
                    }
                }
                sender.sendMessage(ChatColor.YELLOW + "Cleared heap dump directory.");
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Failed to clear heap dump directory. See sever log for details");
                e.printStackTrace();
            }
        });
    }

    @Command(aliases = "heapdump", desc = "Create a heap dump")
    @CommandPermissions({"tgm.command.tgm.heapdump"})
    public static void heapdump(CommandContext context, CommandSender sender) {
        FileConfiguration config = TGM.get().getConfig();
        boolean upload = config.getBoolean("heapdump.upload", false);

        sender.sendMessage(ChatColor.YELLOW + "Generating heap dump...");
        try {
            Path file = dumpHeap(
                    Paths.get(Objects.requireNonNull(config.getString("heapdump.dir", "dumps"))),
                    "dump-" + System.currentTimeMillis()
            );
            if (upload) {
                sender.sendMessage(ChatColor.GREEN + "Uploading heap dump...");
                Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                    try {
                        String code = getUploader().upload(file);
                        String result = uploader.getUrl() + "/" + code;
                        sender.spigot().sendMessage(successUrl(result));
                    } catch (Exception e) {
                        sender.sendMessage(ChatColor.RED + "Failed to upload heap dump, see sever log for details");
                        e.printStackTrace();
                        return;
                    }
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                sender.sendMessage(ChatColor.GREEN + "Generated heap dump at " + file.toString());
            }
        } catch (Throwable throwable) {
            sender.sendMessage(ChatColor.RED + "Failed to write heap dump, see sever log for details");
        }
    }

    public static class TGMCommandNode {

        @Command(aliases = {"tgm"}, desc = "Get essential server info.")
        @CommandPermissions({"tgm.command.tgm"})
        @NestedCommand(value = TGMCommand.class, executeBody = true)
        public static void tgm(CommandContext context, CommandSender sender) {
            String uptime = Strings.getFullAgo(TGM.get().getStartupTime());

            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "SERVER INFO");
            sender.sendMessage(String.format("%sUptime: %s%s", ChatColor.GRAY, ChatColor.WHITE, uptime));
            sender.sendMessage(String.format("%sMemory usage: (%s%s/%s%1$s):", ChatColor.GRAY, ChatColor.WHITE, ServerUtil.getFormattedUsedMemory(), ServerUtil.getFormattedTotalMemory()));
            sender.sendMessage(String.format("%sLoaded worlds (%s%d%1$s):", ChatColor.GRAY, ChatColor.WHITE, Bukkit.getWorlds().size()));
            Bukkit.getWorlds().forEach(w -> sender.sendMessage(ChatColor.GRAY + " - " + ChatColor.WHITE + w.getName()));

            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "TGM INFO");
            sender.sendMessage(String.format("%sMatch number: %s%s", ChatColor.GRAY, ChatColor.WHITE, TGM.get().getMatchManager().getMatchNumber()));
            sender.sendMessage(String.format("%sPlayer contexts: %s%s%s", ChatColor.GRAY, ChatColor.WHITE, TGM.get().getPlayerManager().getPlayers().size(),
                    TGM.get().getPlayerManager().getPlayers().size() != Bukkit.getOnlinePlayers().size() ? ChatColor.RED + "" + ChatColor.BOLD + " !" : ""));
            sender.sendMessage(String.format("%sModules loaded: %s%s", ChatColor.GRAY, ChatColor.WHITE, TGM.get().getMatchManager().getMatch().getModules().size()));
            sender.sendMessage(String.format("%sCached profiles: %s%s", ChatColor.GRAY, ChatColor.WHITE, ProfileCache.getInstance().size()));
        }

    }

    private static TextComponent successUrl(String url) {
        TextComponent message = new TextComponent("Success: ");
        message.setColor(ChatColor.GREEN);
        TextComponent urlComponent = new TextComponent(url);
        urlComponent.setColor(ChatColor.YELLOW);
        urlComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        message.addExtra(urlComponent);
        return message;
    }

    // Stripped from Paper source
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Path dumpHeap(Path dir, String name) throws Throwable {
        java.nio.file.Files.createDirectories(dir);

        javax.management.MBeanServer server = java.lang.management.ManagementFactory.getPlatformMBeanServer();
        Path file;

        try {
            Class clazz = Class.forName("openj9.lang.management.OpenJ9DiagnosticsMXBean");
            Object openj9Mbean = java.lang.management.ManagementFactory.newPlatformMXBeanProxy(server, "openj9.lang.management:type=OpenJ9Diagnostics", clazz);
            java.lang.reflect.Method m = clazz.getMethod("triggerDumpToFile", String.class, String.class);
            file = dir.resolve(name + ".phd");
            m.invoke(openj9Mbean, "heap", file.toString());
        } catch (ClassNotFoundException e) {
            Class clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
            Object hotspotMBean = java.lang.management.ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", clazz);
            java.lang.reflect.Method m = clazz.getMethod("dumpHeap", String.class, boolean.class);
            file = dir.resolve(name + ".hprof");
            m.invoke(hotspotMBean, file.toString(), true);
        }

        return file;
    }

}
