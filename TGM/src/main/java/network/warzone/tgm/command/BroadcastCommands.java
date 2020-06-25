package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import network.warzone.tgm.TGM;
import network.warzone.tgm.broadcast.BroadcastManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Jorge on 4/20/2018.
 */
public class BroadcastCommands {

    @Command(aliases = {"broadcast", "bc"}, desc = "Broadcast tool", usage = "(list|preset|raw|playerpreset|playerraw|config|start|stop|reload)", min = 1)
    @CommandPermissions({"tgm.broadcast"})
    public static void broadcast(CommandContext cmd, CommandSender sender) {
        BroadcastManager broadcastManager = TGM.get().getBroadcastManager();
        if ("preset".equalsIgnoreCase(cmd.getString(0))) {
            if (cmd.argsLength() <= 1) {
                sender.sendMessage(ChatColor.RED + "/" + cmd.getCommand() + " preset <id> [args]");
                return;
            }
            if (!broadcastManager.broadcast(cmd.getString(1), (cmd.argsLength() > 2 ? cmd.getParsedSlice(2) : new String[]{}))) sender.sendMessage(ChatColor.RED + "Broadcast not found.");
        } else if ("raw".equalsIgnoreCase(cmd.getString(0))) {
            if (cmd.argsLength() <= 1) {
                sender.sendMessage(ChatColor.RED + "/" + cmd.getCommand() + " raw <message>");
                return;
            }
            broadcastManager.broadcastRaw(cmd.getRemainingString(1));
        } else if ("ppreset".equalsIgnoreCase(cmd.getString(0)) || "playerpreset".equalsIgnoreCase(cmd.getString(0))) {
            if (cmd.argsLength() <= 1) {
                sender.sendMessage(ChatColor.RED + "/" + cmd.getCommand() + " playerpreset <player> <id> [args]");
                return;
            }
            Player target = Bukkit.getPlayer(cmd.getString(1));
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found \"" + cmd.getString(1) + "\".");
                return;
            }
            if (!broadcastManager.broadcast(target, cmd.getString(2), (cmd.argsLength() > 3 ? cmd.getParsedSlice(3) : new String[]{}))) sender.sendMessage(ChatColor.RED + "Broadcast not found.");
        } else if ("praw".equalsIgnoreCase(cmd.getString(0)) || "playerraw".equalsIgnoreCase(cmd.getString(0))) {
            if (cmd.argsLength() <= 2) {
                sender.sendMessage(ChatColor.RED + "/" + cmd.getCommand() + " playerraw <player> <message>");
                return;
            }
            Player target = Bukkit.getPlayer(cmd.getString(1));
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found \"" + cmd.getString(1) + "\".");
                return;
            }
            broadcastManager.broadcastRaw(target, cmd.getRemainingString(2));
        } else if ("list".equalsIgnoreCase(cmd.getString(0))) {
            if (!broadcastManager.getBroadcasts().isEmpty()) {
                sender.sendMessage(ChatColor.YELLOW + "Presets: ");
                broadcastManager.getBroadcasts().forEach(broadcast -> sender.sendMessage(
                        ChatColor.GRAY + " - " + ChatColor.GREEN + broadcast.getId() + ChatColor.GRAY + ": \"" + ChatColor.RESET + broadcast.getMessage().replace("\n", "\\n") + ChatColor.GRAY + "\""
                ));
            } else {
                sender.sendMessage(ChatColor.YELLOW + "No broadcast presets defined.");
            }
        } else if ("config".equalsIgnoreCase(cmd.getString(0))) {
            if (cmd.argsLength() <= 1) {
                sender.sendMessage(ChatColor.RED + "/" + cmd.getCommand() + " config <autobroadcast|interval|url|queue> [value]");
                return;
            }
            if ("autobroadcast".equalsIgnoreCase(cmd.getString(1))) {
                if (cmd.argsLength() > 2 && !broadcastManager.setAutobroadcast(Boolean.parseBoolean(cmd.getString(2)))) {
                    sender.sendMessage(ChatColor.RED + "Could not set autobroadcast.");
                    return;
                }
                sender.sendMessage(ChatColor.AQUA + "Autobroadcast: " + (broadcastManager.isAutoBroadcast()? ChatColor.GREEN : ChatColor.RED) + broadcastManager.isAutoBroadcast());

            } else if ("interval".equalsIgnoreCase(cmd.getString(1))) {
                if (cmd.argsLength() > 2 && !broadcastManager.setInterval(Integer.parseInt(cmd.getString(2)))) {
                    sender.sendMessage(ChatColor.RED + "Could not set interval.");
                    return;
                }
                sender.sendMessage(ChatColor.AQUA + "Interval: " + ChatColor.GREEN + broadcastManager.getInterval());

            } else if ("url".equalsIgnoreCase(cmd.getString(1))) {
                if (cmd.argsLength() > 2 && !broadcastManager.setURL("-".equals(cmd.getString(2)) ? null : cmd.getString(2))) {
                    sender.sendMessage(ChatColor.RED + "Could not set broadcasts url.");
                    return;
                }
                sender.sendMessage(ChatColor.AQUA + "Broadcasts URL: " + ChatColor.GREEN + broadcastManager.getUrl());

            } else if ("queue".equalsIgnoreCase(cmd.getString(1))) {
                if (cmd.argsLength() > 2 && !broadcastManager.setQueue("-".equals(cmd.getString(2)) ? Collections.emptyList() : Arrays.asList(cmd.getParsedSlice(2)))) {
                    sender.sendMessage(ChatColor.RED + "Could not set autobroadcast queue.");
                    return;
                }
                sender.sendMessage(ChatColor.AQUA + "Broadcast queue: " + ChatColor.GRAY + "[" + ChatColor.RESET + String.join(ChatColor.GRAY + ", " + ChatColor.RESET, broadcastManager.getQueue()) + ChatColor.GRAY + "]");
            } else {
                sender.sendMessage(ChatColor.RED + "Unknown setting.");
            }
        } else if ("reload".equalsIgnoreCase(cmd.getString(0))) {
            broadcastManager.reload();
            sender.sendMessage(ChatColor.GREEN + "Broadcast configuration & list reloaded.");

        } else if ("stop".equalsIgnoreCase(cmd.getString(0))) {
            if (broadcastManager.getTask() == null) {
                sender.sendMessage(ChatColor.RED + "Autobroadcast task not running.");
                return;
            }
            broadcastManager.stopTask();
            sender.sendMessage(ChatColor.YELLOW + "Stopped auto-broadcasting.");

        } else if ("start".equalsIgnoreCase(cmd.getString(0))) {
            if (broadcastManager.getTask() != null) {
                sender.sendMessage(ChatColor.RED + "Autobroadcast task already running.");
                return;
            }
            broadcastManager.startAutoBroadcast(true);
            sender.sendMessage(ChatColor.GREEN + "Started auto-broadcasting.");

        } else {
            sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
        }
    }

}
