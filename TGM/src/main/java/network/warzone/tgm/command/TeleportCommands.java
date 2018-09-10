package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class TeleportCommands {

    @Command(aliases = {"rtp"}, desc = "Teleport players.", usage = "<player> [player]", min = 1, max = 4)
    public static void teleportCommand(final CommandContext cmd, CommandSender sender) throws CommandException {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (cmd.argsLength() == 1) {
                if (sender.hasPermission("tgm.teleport")) {
                    try {
                        Player target = Bukkit.getPlayer(cmd.getString(0));
                        player.teleport(target);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1);
                    } catch (NullPointerException e) {
                        sender.sendMessage(ChatColor.RED + "Player not found");
                    }
                } else {
                    throw new CommandPermissionsException();
                }
            } else if (cmd.argsLength() == 2) {
                if (sender.hasPermission("tgm.teleport")) {
                    try {
                        Player from = Bukkit.getPlayer(cmd.getString(0));
                        Player to = Bukkit.getPlayer(cmd.getString(1));
                        from.teleport(to);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1);
                        if (player != from) {
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1);
                        }
                    } catch (NullPointerException e) {
                        sender.sendMessage(ChatColor.RED + "Player not found");
                    }
                } else {
                    throw new CommandPermissionsException();
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "/teleport <player> [player]");
        }
    }
}
