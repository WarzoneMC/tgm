package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandNumberFormatException;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.modules.knockback.KnockbackSettings;
import network.warzone.tgm.util.Players;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

}
