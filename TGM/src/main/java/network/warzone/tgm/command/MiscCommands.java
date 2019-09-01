package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandNumberFormatException;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.knockback.KnockbackSettings;
import network.warzone.tgm.nickname.NickedUserProfile;
import network.warzone.tgm.util.HashMaps;
import network.warzone.tgm.util.Players;
import network.warzone.warzoneapi.models.Rank;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
                if (!newName.matches("^[a-z_A-Z0-9]+$")) {
                    sender.sendMessage(ChatColor.RED + "Invalid name.");
                    return;
                }
                if (TGM.get().getNickManager().originalNames.values().stream().anyMatch((String s1) -> s1.equals(newName)) ||
                        Bukkit.getOnlinePlayers().stream().anyMatch((Player p1) -> p1.getName().equals(newName))) {
                    sender.sendMessage(ChatColor.RED + "You cannot nick as an online player.");
                    return;
                }

                boolean uuidSpoof = false;
                if (cmd.argsLength() > 2) {
                    uuidSpoof = cmd.getString(2).equals("true");
                    sender.sendMessage(ChatColor.GOLD + "UUID Spoofing: " + uuidSpoof);
                }
                TGM.get().getNickManager().setNick(p, newName, uuidSpoof, null);
                sender.sendMessage(ChatColor.GREEN + "Updated username to " + ChatColor.YELLOW + newName);
            } else if (option.equals("reset")) {
                String original = TGM.get().getNickManager().originalNames.get(p.getUniqueId());
                if (original != null) {
                    TGM.get().getNickManager().reset(p);
                    sender.sendMessage(ChatColor.GREEN + "Reset username");
                } else {
                    sender.sendMessage(ChatColor.RED + "You are not nicked!");
                }
            } else if (option.equals("skin") && cmd.argsLength() > 1) {
                String newName = cmd.getString(1);
                if (newName.length() > 16) {
                    sender.sendMessage(ChatColor.RED + "New skin name must be shorter than 16 characters.");
                    return;
                }
                TGM.get().getNickManager().setSkin(p, newName, null);
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
                if (TGM.get().getNickManager().originalNames.values().stream().anyMatch((String s1) -> s1.equals(newName)) ||
                        Bukkit.getOnlinePlayers().stream().anyMatch((Player p1) -> p1.getName().equals(newName))) {
                    sender.sendMessage(ChatColor.RED + "You cannot nick as an online player.");
                    return;
                }
                TGM.get().getNickManager().setName(p, newName, false, null);
                sender.sendMessage(ChatColor.GREEN + "Updated username to " + ChatColor.YELLOW + newName);
            } else if (option.equals("stats") && cmd.argsLength() > 1) {
                if (cmd.argsLength() > 2) {
                    try {
                        String statToChange = cmd.getString(1);
                        int value = cmd.getInteger(2);

                        switch (statToChange) {
                            case "kills":
                                TGM.get().getNickManager().setStats(p, value, null, null, null, null);
                                break;
                            case "deaths":
                                TGM.get().getNickManager().setStats(p, null, value, null, null, null);
                                break;
                            case "wins":
                                TGM.get().getNickManager().setStats(p, null, null, value, null, null);
                                break;
                            case "losses":
                                TGM.get().getNickManager().setStats(p, null, null, null, value, null);
                                break;
                            case "objectives":
                                TGM.get().getNickManager().setStats(p, null, null, null, null, value);
                                break;
                            default:
                                sender.sendMessage(ChatColor.RED + "Invalid stat. /nick stats <kills|deaths|wins|losses|objectives> <value>");
                                return;
                        }
                        sender.sendMessage(ChatColor.GREEN + "Updated nicked " + ChatColor.YELLOW + statToChange + ChatColor.GREEN + " to " + ChatColor.YELLOW + value + ChatColor.GREEN + ".");
                    } catch (CommandNumberFormatException e) {
                        e.printStackTrace();
                    }
                } else {
                    String type = cmd.getString(1);
                    switch (type) {
                        case "random":
                            TGM.get().getNickManager().setStats(p, generateNumber(10, 100), generateNumber(45, 90), generateNumber(10, 30), generateNumber(10, 30), generateNumber(10, 20));
                            break;
                        case "good":
                            TGM.get().getNickManager().setStats(p, generateNumber(100, 200), generateNumber(35, 75), generateNumber(50, 100), generateNumber(25, 50), generateNumber(50, 100));
                            break;
                        case "bad":
                            TGM.get().getNickManager().setStats(p, generateNumber(10, 100), generateNumber(100, 150), generateNumber(20, 50), generateNumber(50, 150), generateNumber(1, 10));
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
                    if (profile.getHighestRank() != null) {
                        profile.removeRank(profile.getHighestRank());
                    }
                    TGM.get().getNickManager().stats.put(p.getUniqueId(), profile);
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

    @Command(aliases={"whois"}, desc ="Reveals a nicked player")
    public static void whois(CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() > 0) {
            String username = cmd.getString(0).toLowerCase();
            if (TGM.get().getNickManager().nickNames.containsValue(username)) {
                UUID uuid = HashMaps.reverseGetFirst(username, TGM.get().getNickManager().nickNames);
                String originalName = TGM.get().getNickManager().originalNames.get(uuid);
                sender.sendMessage(ChatColor.YELLOW + username + ChatColor.GREEN + " is " + ChatColor.YELLOW + originalName);
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

    public static int generateNumber(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
