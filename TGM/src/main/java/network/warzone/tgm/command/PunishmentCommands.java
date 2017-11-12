package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


/**
 * Created by michael on 11-11-2017.
 */
public class PunishmentCommands {

    @Command(aliases = {"kick", "k"}, desc = "Kick a player.", usage = "<player> [reason]", min = 1)
    @CommandPermissions("tgm.kick")
    public static void kick(CommandContext cmd, CommandSender sender) throws CommandException {
        Player kicked = Bukkit.getPlayer(cmd.getString(0));
        if (kicked == null) {
            sender.sendMessage(ChatColor.RED + "Error: can't find the player.");
        }
        if (!sender.isOp() && kicked.isOp()) {
            sender.sendMessage(ChatColor.RED + "I'm sorry but you cannot kick this person.");
        }
        String reason = cmd.argsLength() > 1 ? cmd.getJoinedStrings(1) : "You have been kicked!";
        Bukkit.broadcastMessage(ChatColor.AQUA + sender.getName() + ChatColor.GOLD + " \u00BB " + ChatColor.RED + "Kicked" + ChatColor.GOLD + " \u00BB " + ChatColor.AQUA + kicked.getName() + ChatColor.GOLD + " \u00BB " + ChatColor.YELLOW + reason);
        kicked.kickPlayer(ChatColor.RED + "You have been kicked from Warzone \n Kicked By: " + ChatColor.AQUA + sender.getName() + "\n" + ChatColor.RED + "Reason: " + ChatColor.AQUA + reason);
    }



    @Command(aliases = {"warn", "w"}, desc = "Warn a player.", usage = "<player> [reason]", min = 1)
    @CommandPermissions("tgm.warn")
    public static void warn(CommandContext cmd, CommandSender sender) throws CommandException {
        Player warned = Bukkit.getPlayer(cmd.getString(0));
        if (warned == null) {
            sender.sendMessage(ChatColor.RED + "Error: can't find the player.");
        }
        String reason = cmd.argsLength() > 1 ? cmd.getJoinedStrings(1) : "You have been warned!";
        //Bukkit.broadcastMessage(ChatColor.AQUA + sender.getName() + ChatColor.GOLD + " \u00BB " + ChatColor.RED + "Warned" + ChatColor.GOLD + " \u00BB " + ChatColor.AQUA + warned.getName() + ChatColor.GOLD + " \u00BB " + reason);
        warned.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "------------------------------");
        warned.sendMessage(ChatColor.RED + "You have been warned for the following reason");
        warned.sendMessage(ChatColor.RED + "Warned by: " + ChatColor.GOLD + sender.getName());
        warned.sendMessage(ChatColor.RED + "Reason: " + ChatColor.GOLD + reason);
        warned.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "------------------------------");
        warned.playSound(warned.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1, 0); // Added Sound when a player gets warned (Suggested by @Vice)
        // Maybe i should add playerTeam.getColor() + module.getPlayerName()
        Bukkit.broadcast(ChatColor.GRAY + "[" + ChatColor.YELLOW + sender.getName() + ChatColor.GRAY + ": Warned " + warned.getDisplayName(), Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
    }
}
