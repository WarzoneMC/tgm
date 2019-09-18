package network.warzone.tgm.command;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandNumberFormatException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.nickname.NickManager;
import network.warzone.tgm.nickname.NickedUserProfile;
import network.warzone.tgm.util.HashMaps;
import network.warzone.tgm.util.Players;
import network.warzone.warzoneapi.models.Rank;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/*
These commands MUST NOT use ANY API FUNCTIONALITY. This class will
be enabled WHETHER THE API IS ENABLED OR NOT.
 */

public class MiscCommands {

    @Command(aliases = {"ping"}, desc = "Check player ping", max = 1, usage = "(name)")
    public static void ping(CommandContext cmd, CommandSender sender) {
        Player player;
        if (cmd.argsLength() > 0) {
            player = Bukkit.getPlayer(cmd.getString(0));
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + cmd.getString(0));
                return;
            }
        } else if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            sender.sendMessage(ChatColor.RED + "As console, you can use /ping <player> to check someone's ping.");
            return;
        }
        int playerPing = Players.getPing(player);
        String pingMsg = ((playerPing >= 0) ? (ChatColor.AQUA + player.getName() + ChatColor.GRAY + "'" + (player.getName().endsWith("s") ? "" : "s") + " ping is " + ChatColor.AQUA + playerPing + "ms") : ChatColor.RED + "Could not get ping.");
        sender.sendMessage(pingMsg);
    }

    @Command(aliases = {"nicks"}, desc = "View all nicked players")
    @CommandPermissions({"tgm.command.nicks"})
    public static void nicks(CommandContext cmd, CommandSender sender) {
        HashMap<UUID, String> originalNames = TGM.get().getNickManager().getOriginalNames();
        HashMap<UUID, String> nickNames = TGM.get().getNickManager().getNickNames();

        if (originalNames.size() == 0) {
            sender.sendMessage(ChatColor.RED + "No nicked players found.");
            return;
        }

        StringBuilder message = new StringBuilder(ChatColor.YELLOW + "Nicked Players:");

        originalNames.forEach((uuid, originalName) -> {
            message.append("\n")
                    .append(ChatColor.DARK_PURPLE)
                    .append(originalName)
                    .append(ChatColor.GRAY)
                    .append(" is nicked as ")
                    .append(ChatColor.LIGHT_PURPLE)
                    .append(nickNames.get(uuid));
        });

        sender.sendMessage(message.toString());
    }

    @Command(aliases = {"nick"}, desc = "Change nickname", usage = "(name)")
    @CommandPermissions({"tgm.command.nick"})
    public static void nick(CommandContext cmd, CommandSender sender) throws IllegalAccessException, NoSuchFieldException, UnirestException {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player only command.");
            return;
        }
        Player p = (Player) sender;
        if (cmd.argsLength() > 0) {
            String option = cmd.getString(0);
            if (option.equals("set") && cmd.argsLength() > 1) {
                String newName = cmd.getString(1);
                if (newName.length() > 16) {
                    sender.sendMessage(ChatColor.RED + "Username must be shorter than 16 characters.");
                    return;
                }
                if (!newName.matches("^[a-z_A-Z0-9]+$")) {
                    sender.sendMessage(ChatColor.RED + "Invalid name.");
                    return;
                }
                if (
                        Bukkit.getOnlinePlayers().stream().map(
                                (Player player) -> TGM.get().getPlayerManager().getPlayerContext(player).getOriginalName()
                        ).anyMatch((String name) -> name.equals(newName))) {
                    sender.sendMessage(ChatColor.RED + "You cannot nick as an online player.");
                    return;
                }
                boolean force = false;
                if (cmd.argsLength() > 2) {
                    String arg2 = cmd.getString(2);
                    force = arg2.equals("force");
                }
                try {
                    if (force) {
                        try {
                            TGM.get().getNickManager().setNick(p, newName, null);
                        } catch (UnirestException | NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        TGM.get().getNickManager().addQueuedNick(p, newName);
                    }
                } catch (UnirestException e) {
                    p.sendMessage(NickManager.RATELIMITED_MESSAGE);
                }

                sender.sendMessage(ChatColor.GREEN + "Updated username to " + ChatColor.YELLOW + newName);
                if (!force) {
                    sender.sendMessage(ChatColor.GOLD + "Reconnect for the changes to apply!");
                }
            } else if (option.equals("reset")) {
                String original = TGM.get().getNickManager().getOriginalNames().get(p.getUniqueId());
                if (original != null) {
                    TGM.get().getNickManager().reset(p, true);
                } else {
                    sender.sendMessage(ChatColor.RED + "You are not nicked!");
                }
            } else if (option.equals("skin") && cmd.argsLength() > 1) {
                String newName = cmd.getString(1);
                if (newName.length() > 16) {
                    sender.sendMessage(ChatColor.RED + "Username must be shorter than 16 characters.");
                    return;
                }
                try {
                    TGM.get().getNickManager().setSkin(p, newName, null);
                } catch (UnirestException e) {
                    p.sendMessage(NickManager.RATELIMITED_MESSAGE);
                }
                sender.sendMessage(ChatColor.GREEN + "Updated skin to " + ChatColor.YELLOW + newName);
            } else if (option.equals("name") && cmd.argsLength() > 1) {
                String newName = cmd.getString(1);

                if (!newName.matches("^[a-z_A-Z0-9]+$")) {
                    sender.sendMessage(ChatColor.RED + "Invalid name.");
                    return;
                }
                if (newName.length() > 16) {
                    sender.sendMessage(ChatColor.RED + "New name must be shorter than 16 characters.");
                    return;
                }
                if (
                        Bukkit.getOnlinePlayers().stream().map(
                                (Player player) -> TGM.get().getPlayerManager().getPlayerContext(player).getOriginalName()
                        ).anyMatch((String name) -> name.equals(newName))) {
                    sender.sendMessage(ChatColor.RED + "You cannot nick as an online player.");
                    return;
                }
                try {
                    TGM.get().getNickManager().setName(p, newName);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    p.sendMessage(NickManager.RATELIMITED_MESSAGE);
                }
                sender.sendMessage(ChatColor.GREEN + "Updated username to " + ChatColor.YELLOW + newName);
            } else if (option.equals("stats") && cmd.argsLength() > 1) {
                if (cmd.argsLength() > 2) {
                    try {
                        String statToChange = cmd.getString(1);
                        int value = cmd.getInteger(2);

                        try {
                            TGM.get().getNickManager().setStats(p, statToChange, value);
                        } catch (NoSuchFieldException e) {
                            sender.sendMessage(ChatColor.RED + "Invalid stat. /nick stats <kills|deaths|wins|losses|objectives> <value>");
                        }
                        sender.sendMessage(ChatColor.GREEN + "Updated nicked " + ChatColor.YELLOW + statToChange + ChatColor.GREEN + " to " + ChatColor.YELLOW + value + ChatColor.GREEN + ".");
                    } catch (CommandNumberFormatException e) {
                        e.printStackTrace();
                    }
                } else {
                    String type = cmd.getString(1);
                    switch (type) {
                        case "random":
                            TGM.get().getNickManager().setStats(p, generateInt(10, 100), generateInt(45, 90), generateInt(10, 30), generateInt(10, 30), generateInt(10, 20));
                            break;
                        case "good":
                            double goodKDR = generateDouble(1, 2);
                            int goodDeaths = generateInt(35, 75);
                            TGM.get().getNickManager().setStats(p, (int) (goodDeaths * goodKDR), goodDeaths, generateInt(50, 100), generateInt(25, 50), generateInt(50, 100));
                            break;
                        case "bad":
                            double badKDR = generateDouble(0.1, 1);
                            int badDeaths = generateInt(100, 150);
                            TGM.get().getNickManager().setStats(p, (int) (badDeaths * badKDR), badDeaths, generateInt(20, 50), generateInt(50, 150), generateInt(1, 10));
                            break;
                        default:
                            sender.sendMessage(ChatColor.RED + "/nick stats <statName|good|random|bad> [value]");
                            return;
                    }
                    sender.sendMessage(ChatColor.GREEN + "Set stats to preset ranges " + ChatColor.YELLOW + type);
                }
            } else if (option.equals("rank") && cmd.argsLength() > 1) {
                String newRank = cmd.getString(1);
                Rank rank = null;
                for (Rank r : TGM.get().getTeamClient().retrieveRanks()) {
                    if (r.getName().equalsIgnoreCase(newRank)) rank = r;
                }
                if (newRank.equals("none")) {
                    NickedUserProfile profile = TGM.get().getNickManager().getUserProfile(p);
                    profile.setRanks(Collections.emptyList());
                    TGM.get().getNickManager().getStats().put(p.getUniqueId(), profile);
                    sender.sendMessage(ChatColor.GREEN + "Removed nicked rank");
                    return;
                }
                if (rank != null) {
                    TGM.get().getNickManager().setRank(p, rank);
                    sender.sendMessage(ChatColor.GREEN + "Updated rank to " + ChatColor.YELLOW + rank.getName());
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid rank");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "/nick <set|reset|name|skin|stats|rank> <option>");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "/nick <set|reset|name|skin|stats|rank> <option>");
        }
    }

    @Command(aliases = {"whois"}, desc = "Reveals a nicked player")
    @CommandPermissions({"tgm.command.whois"})
    public static void whois(CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() > 0) {
            String username = cmd.getString(0);
            if (TGM.get().getNickManager().getNickNames().containsValue(username)) {
                UUID uuid = HashMaps.reverseGetFirst(username, TGM.get().getNickManager().getNickNames());
                String originalName = TGM.get().getNickManager().getOriginalNames().get(uuid);
                sender.sendMessage(ChatColor.YELLOW + username + ChatColor.GREEN + " is " + ChatColor.YELLOW + originalName);
            } else {
                sender.sendMessage(ChatColor.RED + "That user isn't nicked!");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid user.");
        }
    }

    public static double generateDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max + 1);
    }

    public static int generateInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
