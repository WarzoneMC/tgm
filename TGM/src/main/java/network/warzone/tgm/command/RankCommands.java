package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandNumberFormatException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import network.warzone.tgm.TGM;
import network.warzone.tgm.util.Ranks;
import network.warzone.warzoneapi.models.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Jorge on 2/23/2018.
 */
public class RankCommands {

    @Command(aliases = {"rank", "ranks"}, desc = "Rank management command.", min = 1, usage = "(player|list|info|create|delete|edit|permissions)")
    @CommandPermissions({"tgm.command.rank"})
    public static void rank(CommandContext cmd, CommandSender sender) {
        if (cmd.getString(0).equalsIgnoreCase("player")) {
            if (cmd.argsLength() < 4) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " player <player|UUID> <add|remove> <rank>");
                return;
            }

            RankUpdateRequest.Action action = RankUpdateRequest.Action.byName(cmd.getString(2).toUpperCase());
            if (action == null) {
                sender.sendMessage(ChatColor.RED + "Unknown action: " + cmd.getString(2));
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " player <player|UUID> <add|remove> <rank>");
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                RankUpdateResponse response = TGM.get().getTeamClient().updateRank(cmd.getString(1), action, new RankUpdateRequest(cmd.getString(3)));
                if (response.isError()) {
                    sender.sendMessage(ChatColor.RED + response.getMessage());
                    return;
                }

                Player target;
                if (cmd.getString(1).length() == 36) target = Bukkit.getPlayer(UUID.fromString(cmd.getString(1)));
                else target = Bukkit.getPlayer(cmd.getString(1));
                switch (action) {
                    case ADD:
                        if (target != null) {
                            TGM.get().getPlayerManager().getPlayerContext(target).getUserProfile().addRank(response.getRank());
                            TGM.get().getPlayerManager().getPlayerContext(target).updateRank(response.getRank());
                        }
                        sender.sendMessage(ChatColor.GRAY + "Added rank " + ChatColor.RESET + response.getRank().getName() + ChatColor.GRAY + " to " + ChatColor.RESET + (target != null ? target.getName() : cmd.getString(1)));
                        break;
                    case REMOVE:
                        if (target != null) {
                            TGM.get().getPlayerManager().getPlayerContext(target).getUserProfile().removeRank(response.getRank());
                            TGM.get().getPlayerManager().getPlayerContext(target).updateRank(response.getRank(), true);
                        }
                        sender.sendMessage(ChatColor.GRAY + "Removed rank " + ChatColor.RESET + response.getRank().getName() + ChatColor.GRAY + " from " + ChatColor.RESET + (target != null ? target.getName() : cmd.getString(1)));
                        break;
                }
            });

        } else if (cmd.getString(0).equalsIgnoreCase("list")) {

            Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                sender.sendMessage(ChatColor.YELLOW + "Registered ranks:");
                for (Rank rank : TGM.get().getTeamClient().retrieveRanks()) {
                    sender.spigot().sendMessage(rankToTextComponent(rank));
                }
            });

        } else if (cmd.getString(0).equalsIgnoreCase("info")) {
            if (cmd.argsLength() < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " info <rank Name>");
                return;
            }
            String name = cmd.getString(1);
            Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                for (Rank rank : TGM.get().getTeamClient().retrieveRanks()) {
                    if (rank.getName().equalsIgnoreCase(name)){
                        sender.sendMessage(ChatColor.YELLOW + "Rank info for " + ChatColor.GRAY + rank.getName());
                        sender.sendMessage(ChatColor.GRAY + "ID: " + ChatColor.RESET + rank.getId().toString());
                        sender.sendMessage(ChatColor.GRAY + "Name: " + ChatColor.RESET + rank.getName());
                        sender.sendMessage(ChatColor.GRAY + "Prefix: " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', rank.getPrefix()));
                        sender.sendMessage(ChatColor.GRAY + "Priority: " + ChatColor.RESET + rank.getPriority());
                        sender.sendMessage(ChatColor.GRAY + "Staff: " + ChatColor.RESET + rank.isStaff());
                        sender.sendMessage(ChatColor.GRAY + "Permissions: ");
                        for (String permission : rank.getPermissions()) {
                            sender.spigot().sendMessage(permissionToTextComponent(rank.getName(), permission));
                        }
                        break;
                    }
                    sender.sendMessage(ChatColor.RED + "Rank not found.");
                }
            });

        } else if (cmd.getString(0).equalsIgnoreCase("create")) {

            if (cmd.argsLength() < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " create <name> [prefix] [staff(true|false)] [priority]");
                return;
            }

            String name = cmd.getString(1);
            String prefix = cmd.argsLength() >= 3 ? cmd.getString(2) : null;
            boolean staff = cmd.argsLength() >= 4 ? Boolean.valueOf(cmd.getString(3)) : null;
            int priority = 0;
            try {
                priority = cmd.argsLength() >= 5 ? cmd.getInteger(4) : 0;
            } catch (CommandNumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Unknown priority \"" + cmd.getString(0) + "\"");
                return;
            }
            RankManageRequest request = new RankManageRequest(name, priority, prefix, new ArrayList<>(), staff);

            Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                RankManageResponse response = TGM.get().getTeamClient().manageRank(RankManageRequest.Action.CREATE, request);
                if (response.isError()) {
                    sender.sendMessage(ChatColor.RED + response.getMessage());
                    return;
                } else {
                    sender.sendMessage(ChatColor.GRAY + "Created rank " + ChatColor.RESET + response.getRank().getName());
                }
            });

        } else if (cmd.getString(0).equalsIgnoreCase("delete")) {
            if (cmd.argsLength() < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " delete <name>");
                return;
            }
            String name = cmd.getString(1);
            Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                RankManageResponse response = TGM.get().getTeamClient().manageRank(RankManageRequest.Action.DELETE, new RankManageRequest(name));
                if (response.isError()) {
                    sender.sendMessage(ChatColor.RED + response.getMessage());
                    return;
                } else {
                    sender.sendMessage(ChatColor.GRAY + "Deleted rank " + ChatColor.RESET + response.getRank().getName());
                }
            });

        } else if (cmd.getString(0).equalsIgnoreCase("edit")) {

            if (cmd.argsLength() < 4) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " edit <name> <prefix|staff|priority|permissions> <value>");
                return;
            }
            String name = cmd.getString(1);
            RankEditRequest.EditableField field = RankEditRequest.EditableField.valueOf(cmd.getString(2).toUpperCase());
            Object value = field.parseValue(cmd.getRemainingString(3));

            Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                RankManageResponse response = TGM.get().getTeamClient().editRank(field, new RankEditRequest(name, value));
                if (response.isError()) {
                    sender.sendMessage(ChatColor.RED + response.getMessage());
                    return;
                } else {
                    sender.sendMessage(ChatColor.GRAY + "Set value of " + ChatColor.RESET + field.toString().toLowerCase() + ChatColor.GRAY + " to " + ChatColor.RESET + value.toString() + ChatColor.GRAY + " for " + ChatColor.RESET + name);
                    Ranks.update(response.getRank());
                }
            });

        } else if (cmd.getString(0).equalsIgnoreCase("permissions")) {

            if (cmd.argsLength() < 4) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " permissions <name> <add|remove> <permissions>");
                return;
            }

            String name = cmd.getString(1);
            RankPermissionsUpdateRequest.Action action = RankPermissionsUpdateRequest.Action.valueOf(cmd.getString(2).toUpperCase());
            if (action == null) {
                sender.sendMessage(ChatColor.RED + "Unknown action: " + cmd.getString(2));
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " permissions <name> <add|remove> <permissions>");
                return;
            }
            List<String> permissions = Arrays.asList(cmd.getRemainingString(3).split(" "));

            Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                RankManageResponse response = TGM.get().getTeamClient().editPermissions(action, new RankPermissionsUpdateRequest(name, permissions));
                if (response.isError()) {
                    sender.sendMessage(ChatColor.RED + response.getMessage());
                    return;
                } else {
                    switch (action) {
                        case ADD:
                            sender.sendMessage(ChatColor.GRAY + "Added permissions " + ChatColor.RESET + permissions.toString() + ChatColor.GRAY + " to rank " + ChatColor.RESET + response.getRank().getName());
                            break;
                        case REMOVE:
                            sender.sendMessage(ChatColor.GRAY + "Removed permissions " + ChatColor.RESET + permissions.toString() + ChatColor.GRAY + " from rank " + ChatColor.RESET + response.getRank().getName());
                            break;
                    }
                    Ranks.update(response.getRank());
                }
            });
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
        }
    }

    private static TextComponent rankToTextComponent(Rank rank) {
        TextComponent main = new TextComponent(ChatColor.GRAY + " - " + ChatColor.RESET + rank.getPriority() + ChatColor.GRAY + ": " + ChatColor.RESET + rank.getName() + ChatColor.GRAY + " - " + ChatColor.translateAlternateColorCodes('&', rank.getPrefix()));
        main.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{
                new TextComponent(ChatColor.GRAY + "ID: " + ChatColor.RESET + rank.getId().toString()),
                new TextComponent(ChatColor.GRAY + "\nName: " + ChatColor.RESET + rank.getName()),
                new TextComponent(ChatColor.GRAY + "\nPrefix: " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', rank.getPrefix())),
                new TextComponent(ChatColor.GRAY + "\nPriority: " + ChatColor.RESET + rank.getPriority()),
                new TextComponent(ChatColor.GRAY + "\nStaff: " + ChatColor.RESET + rank.isStaff()),
                new TextComponent(ChatColor.YELLOW + "\n\nClick for full info"),
        }));
        main.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rank info " + rank.getName()));
        return main;
    }

    private static TextComponent permissionToTextComponent(String rank, String permission) {
        TextComponent main = new TextComponent(" - " + permission);
        main.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[] {new TextComponent(ChatColor.YELLOW + "Click to remove.")}));
        main.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rank permissions " + rank + " remove " + permission));
        return main;
    }

}
