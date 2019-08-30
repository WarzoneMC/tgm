package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandNumberFormatException;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.knockback.KnockbackSettings;
import network.warzone.tgm.util.Players;
import network.warzone.warzoneapi.models.Rank;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

/*
These commands MUST NOT use ANY API FUNCTIONALITY. This class will
be enabled WHETHER THE API IS ENABLED OR NOT.
 */

public class MiscCommands {

    @Command(aliases= {"ping"}, desc = "Check player ping", max = 1, usage = "(name)")
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

    @Command(aliases={"setkb"}, desc = "Change the KB", usage = "(amount)")
    public static void setkb(CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() > 0) {
            try {
                double kbMultiplier = cmd.getDouble(0);
                sender.sendMessage(ChatColor.GREEN + "Knockback Modifier updated from " + ChatColor.YELLOW + KnockbackSettings.multiplier + ChatColor.GREEN + " to " + ChatColor.YELLOW + kbMultiplier);
                KnockbackSettings.multiplier = (float) kbMultiplier;
            } catch (CommandNumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid number.");
            }
        } else {
            sender.sendMessage(ChatColor.GREEN + "Current Knockback Modifier: " + ChatColor.WHITE + KnockbackSettings.multiplier);
        }
    }

    @Command(aliases={"setkbheight"}, desc = "Change the knockback height", usage= "(amount)")
    public static void setkbheight(CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() > 0) {
            try {
                double kbHeight = cmd.getDouble(0);
                sender.sendMessage(ChatColor.GREEN + "Knockback Height updated from " + ChatColor.YELLOW + KnockbackSettings.height + ChatColor.GREEN + " to " + ChatColor.YELLOW + kbHeight);
                KnockbackSettings.height = (float) kbHeight;
            } catch (CommandNumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid number.");
            }
        } else {
            sender.sendMessage(ChatColor.GREEN + "Current Knockback Height: " + ChatColor.WHITE + KnockbackSettings.height);
        }
    }

    @Command(aliases={"nick"}, desc= "Change nickname", usage ="(name)")
    public static void nick(CommandContext cmd, CommandSender sender) {
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
                    sender.sendMessage(ChatColor.RED + "New name must be shorter than 16 characters.");
                    return;
                }
                if (TGM.get().getNickManager().originalNames.values().stream().anyMatch((String s1) -> s1.equals(newName)) ||
                        Bukkit.getOnlinePlayers().stream().anyMatch((Player p1) -> p1.getName().equals(newName))) {
                    sender.sendMessage(ChatColor.RED + "You cannot nick as an online player.");
                    return;
                }
                TGM.get().getNickManager().setName(p, newName);
                TGM.get().getNickManager().setSkin(p, newName);
                sender.sendMessage(ChatColor.GREEN + "Updated username to " + ChatColor.YELLOW + newName);
            } else if (option.equals("reset")) {
                String original = TGM.get().getNickManager().originalNames.get(p.getUniqueId());
                if (original != null) {
                    TGM.get().getNickManager().setName(p, original);
                    TGM.get().getNickManager().setSkin(p, original);
                    TGM.get().getNickManager().setLevel(p, TGM.get().getPlayerManager().getPlayerContext(p).getUserProfile().getLevel());
                    TGM.get().getNickManager().setRank(p, TGM.get().getPlayerManager().getPlayerContext(p).getUserProfile().getHighestRank());
                    sender.sendMessage(ChatColor.GREEN + "Reset username");
                }
            } else if (option.equals("skin") && cmd.argsLength() > 1) {
                String newName = cmd.getString(1);
                if (newName.length() > 16) {
                    sender.sendMessage(ChatColor.RED + "New skin name must be shorter than 16 characters.");
                    return;
                }
                TGM.get().getNickManager().setSkin(p, newName);
                sender.sendMessage(ChatColor.GREEN + "Updated skin to " + ChatColor.YELLOW + newName);
            } else if (option.equals("name") && cmd.argsLength() > 1) {
                String newName = cmd.getString(1);

                if (newName.length() > 16) {
                    sender.sendMessage(ChatColor.RED + "New name must be shorter than 16 characters.");
                    return;
                }
                if (TGM.get().getNickManager().originalNames.values().stream().anyMatch((String s1) -> s1.equals(newName)) ||
                        Bukkit.getOnlinePlayers().stream().anyMatch((Player p1) -> p1.getName().equals(newName))) {
                    sender.sendMessage(ChatColor.RED + "You cannot nick as an online player.");
                    return;
                }
                TGM.get().getNickManager().setName(p, newName);
                sender.sendMessage(ChatColor.GREEN + "Updated username to " + ChatColor.YELLOW + newName);
            } else if (option.equals("level") && cmd.argsLength() > 1) {
                try {
                    int newLevel = cmd.getInteger(1);
                    TGM.get().getNickManager().setLevel(p, newLevel);
                    sender.sendMessage(ChatColor.GREEN + "Updated level to " + ChatColor.YELLOW + newLevel);
                } catch (CommandNumberFormatException e) {
                    e.printStackTrace();
                }
            } else if (option.equals("rank") && cmd.argsLength() > 1) {
                String newRank = cmd.getString(1);
                Rank rank = null;
                for (Rank r : TGM.get().getTeamClient().retrieveRanks()) {
                    if (r.getName().equalsIgnoreCase(newRank)) rank = r;
                }
                if (rank != null) {
                    TGM.get().getNickManager().setRank(p, rank);
                    sender.sendMessage(ChatColor.GREEN + "Updated rank to " + ChatColor.YELLOW + rank.getName());
                }
            }
        }
    }

    @Command(aliases={"whois"}, desc ="Reveals a nicked player")
    public static void whois(CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() > 0) {
            String username = cmd.getString(0);
            if (TGM.get().getNickManager().nickNames.containsValue(username)) {
                Optional<Map.Entry<UUID, String>> first = TGM.get().getNickManager().nickNames.entrySet().stream().filter(uuidStringEntry -> uuidStringEntry.getValue().equals(username)).findFirst();
                if (first.isPresent()) {
                    UUID uuid = first.get().getKey();
                    String originalName = TGM.get().getNickManager().originalNames.get(uuid);
                    sender.sendMessage(ChatColor.YELLOW + username + ChatColor.GREEN + " is " + ChatColor.YELLOW + originalName);
                }
            } else {
                sender.sendMessage(ChatColor.RED + "That user isn't nicked!");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid user.");
        }
    }

    @Command(aliases = {"tgm"}, desc = "General TGM command")
    public static void tgm(CommandContext cmd, CommandSender sender) {
        Properties gitInfo = TGM.get().getGitInfo();
        sender.sendMessage(String.format(ChatColor.AQUA + "This server is running TGM version git-%s (latest commit: %s)", gitInfo.getProperty("git.commit.id.abbrev"), gitInfo.getProperty("git.commit.message.short")));
    }
}
