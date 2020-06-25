package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.menu.TagsMenu;
import network.warzone.warzoneapi.models.GetPlayerByNameResponse;
import network.warzone.warzoneapi.models.PlayerTagsUpdateRequest;
import network.warzone.warzoneapi.models.PlayerTagsUpdateResponse;
import network.warzone.warzoneapi.models.UserProfile;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Jorge on 03/22/2020
 */
public class TagCommands {

    @Command(aliases = {"tag", "tags"}, desc = "Manage your tags.")
    public static void tags(final CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players may open the tags GUI.");
                return;
            }
            PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext((Player) sender);
            TagsMenu tagsMenu = new TagsMenu(ChatColor.UNDERLINE + "Your tags", 9 * 6, playerContext);
            tagsMenu.open((Player) sender);
        }
        else {
            if (cmd.getString(0).equalsIgnoreCase("set")) {
                if (cmd.argsLength() < 2) {
                    if (sender.hasPermission("tgm.tags.set"))
                        sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " set <tag> [player]");
                    else
                        sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " set <tag>");
                    return;
                }
                if (cmd.argsLength() < 3 && !(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must include a player.");
                    return;
                }
                String tag;
                if ("-".equals(cmd.getString(1))) tag = null;
                else tag = cmd.getString(1);
                String target;
                if (sender.hasPermission("tgm.tags.set") && cmd.argsLength() >= 3)
                    target = cmd.getString(2);
                else
                    target = sender.getName();
                Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                    PlayerTagsUpdateResponse response = TGM.get().getTeamClient().updateTag(target, tag, PlayerTagsUpdateRequest.Action.SET);

                    if (!response.isError()) {
                        String finalTag = tag == null ? ChatColor.GRAY + "" + ChatColor.ITALIC + "none" : ChatColor.translateAlternateColorCodes('&', response.getActiveTag());
                        Player player;
                        String responsePlayer = response.getPlayer();
                        if (responsePlayer.length() <= 16) {
                            player = Bukkit.getPlayer(response.getPlayer());
                        } else {
                            player = Bukkit.getPlayer(UUID.fromString(response.getPlayer()));
                        }
                        if (player != null) {
                            TGM.get().getPlayerManager().getPlayerContext(player).getUserProfile().saveTags(response);
                            if (player.getName().equalsIgnoreCase(sender.getName()))
                                sender.sendMessage(ChatColor.GREEN + "Set your active tag to " + finalTag);
                            else
                                sender.sendMessage(ChatColor.GREEN + "Set " + player.getName() + "'s active tag to " + finalTag);
                        } else {
                            sender.sendMessage(ChatColor.GREEN + "Set " + responsePlayer + "'s active tag to " + finalTag);
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + response.getMessage());
                    }
                });
            } else if (cmd.getString(0).equalsIgnoreCase("add")) {
                if (!sender.hasPermission("tgm.tags.add")) {
                    sender.sendMessage(ChatColor.RED + "Insufficient permissions.");
                    return;
                }
                if (cmd.argsLength() < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " add <tag> <player|uuid>");
                    return;
                }
                String tag = cmd.getString(1);
                String target = cmd.getString(2);
                Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                    PlayerTagsUpdateResponse response = TGM.get().getTeamClient().updateTag(target, tag, PlayerTagsUpdateRequest.Action.ADD);
                    if (!response.isError()) {
                        Player player;
                        String responsePlayer = response.getPlayer();
                        if (responsePlayer.length() <= 16) {
                            player = Bukkit.getPlayer(response.getPlayer());
                        } else {
                            player = Bukkit.getPlayer(UUID.fromString(response.getPlayer()));
                        }
                        if (player != null) {
                            TGM.get().getPlayerManager().getPlayerContext(player).getUserProfile().saveTags(response);
                            sender.sendMessage(ChatColor.GREEN + "Added tag '" + ChatColor.translateAlternateColorCodes('&', tag) + ChatColor.GREEN + "' to " + player.getName() + "'s profile");
                        } else {
                            sender.sendMessage(ChatColor.GREEN + "Added tag '" + ChatColor.translateAlternateColorCodes('&', tag) + ChatColor.GREEN + "' to " + responsePlayer + "'s profile");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + response.getMessage());
                    }
                });
            } else if (cmd.getString(0).equalsIgnoreCase("remove")) {
                if (!sender.hasPermission("tgm.tags.remove")) {
                    sender.sendMessage(ChatColor.RED + "Insufficient permissions.");
                    return;
                }
                if (cmd.argsLength() < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " remove <tag> <player|uuid>");
                    return;
                }
                String tag = cmd.getString(1);
                String target = cmd.getString(2);
                Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                    PlayerTagsUpdateResponse response = TGM.get().getTeamClient().updateTag(target, tag, PlayerTagsUpdateRequest.Action.REMOVE);

                    if (!response.isError()) {
                        Player player;
                        String responsePlayer = response.getPlayer();
                        if (responsePlayer.length() <= 16) {
                            player = Bukkit.getPlayer(response.getPlayer());
                        } else {
                            player = Bukkit.getPlayer(UUID.fromString(response.getPlayer()));
                        }
                        if (player != null) {
                            TGM.get().getPlayerManager().getPlayerContext(player).getUserProfile().saveTags(response);
                            sender.sendMessage(ChatColor.GREEN + "Removed tag '" + ChatColor.translateAlternateColorCodes('&', tag) + ChatColor.GREEN + "' from " + player.getName() + "'s profile");
                        } else {
                            sender.sendMessage(ChatColor.GREEN + "Removed tag '" + ChatColor.translateAlternateColorCodes('&', tag) + ChatColor.GREEN + "' from " + responsePlayer + "'s profile");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + response.getMessage());
                    }
                });
            } else if (cmd.getString(0).equalsIgnoreCase("list")) {
                if (!sender.hasPermission("tgm.tags.list")) {
                    sender.sendMessage(ChatColor.RED + "Insufficient permissions.");
                    return;
                }
                if (cmd.argsLength() < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " list <player|uuid>");
                    return;
                }
                String target = cmd.getString(1);
                Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                    GetPlayerByNameResponse playerByNameResponse = TGM.get().getTeamClient().player(target);
                    UserProfile profile = playerByNameResponse.getUser();
                    if (profile == null) {
                        sender.sendMessage(ChatColor.RED + "Player not found.");
                        return;
                    }
                    if (profile.getTags() != null && !profile.getTags().isEmpty()) {
                        sender.sendMessage(ChatColor.BLUE + playerByNameResponse.getUser().getName() + "'s tags:");
                        for (String tag : profile.getTags()) {
                            if (tag.equals(profile.getActiveTag()))
                                sender.sendMessage(ChatColor.GREEN + "- " + ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', tag));
                            else
                                sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', tag));
                        }
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "User has no tags.");
                    }
                });
            } else {
                List<String> subcmds = new ArrayList<>();
                if (sender.hasPermission("tgm.tags.set")) subcmds.add("set");
                if (sender.hasPermission("tgm.tags.add")) subcmds.add("add");
                if (sender.hasPermission("tgm.tags.remove")) subcmds.add("remove");
                if (sender.hasPermission("tgm.tags.list")) subcmds.add("list");
                if (subcmds.isEmpty())
                    sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " [set]");
                else
                    sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " <" + String.join("|", subcmds) + ">");
            }
        }
    }

}
