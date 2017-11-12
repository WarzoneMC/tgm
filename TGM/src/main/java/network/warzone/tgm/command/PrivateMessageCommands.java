package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by michael on 12-11-2017.
 */
public class PrivateMessageCommands {


    @Command(aliases = {"pm", "message"}, desc = "Send a private message to a player,", usage = "<player> <message>", min = 2)
    public static void msg(final CommandContext cmd, CommandSender sender) throws CommandException {
        Player target = Bukkit.getPlayer(cmd.getString(0));
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Error: can't find the player.");
        }
        target.sendMessage("§7§oFrom §b" + sender.getName() + ChatColor.GRAY + ": §f" + ChatColor.RESET + cmd.getJoinedStrings(1));
        sender.sendMessage("§7§oTo §b" + target.getName() + ChatColor.GRAY + ": §f" + ChatColor.RESET + cmd.getJoinedStrings(1));
        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2F);
    }
}
