package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
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
        sender.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.GRAY + "'" + (player.getName().endsWith("s") ? "" : "s") + " ping is " + ChatColor.AQUA + player.spigot().getPing() + "ms");
    }

    @Command(aliases = {"playtime", "pt"}, desc = "Check your playtime", max = 1, usage = "(name)")
    public static void playTime(CommandContext cmd, CommandSender sender) {
        Player player;
        if (cmd.argsLength() > 0) {
            player = Bukkit.getPlayer(cmd.getString(0));
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Player not found" + cmd.getString(0));
                return;
            }
        } else if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            sender.sendMessage(ChatColor.RED + "As console, you can use /playtime <player> to check someone's playtime on the server.");
            return;
        }
        sender.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.GRAY + "'" + (player.getName().endsWith("s") ? "" : "s") + " playtime is " + ChatColor.AQUA + player.getStatistic(Statistic.PLAY_ONE_TICK));
    }

}
