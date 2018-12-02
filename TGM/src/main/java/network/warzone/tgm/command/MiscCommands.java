package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/*
These commands MUST NOT use ANY API FUNCTIONALITY. This class will
be enabled WHETHER THE API IS ENABLED OR NOT.
 */

public class MiscCommands {

    @Command(aliases= {"ping"}, desc = "Check player ping", max = 1, usage = "(name)")
    @CommandPermissions({"tgm.ping"})
    public static void ping(CommandContext cmd, CommandSender sender) {
        Player player = (Player) sender;
        if (cmd.argsLength() > 0) {
            try {
                player = Bukkit.getPlayer(cmd.getString(0));
            } catch(Error err) {
                sender.sendMessage(ChatColor.RED + "Invalid player!");
            }
        }
        sender.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.GRAY + "'s ping is " + ChatColor.AQUA + player.spigot().getPing() + "ms");
    }

}
