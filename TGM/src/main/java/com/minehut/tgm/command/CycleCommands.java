package com.minehut.tgm.command;

import com.minehut.tgm.TGM;
import com.minehut.tgm.match.MatchStatus;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class CycleCommands {

    @Command(aliases = {"cycle"}, desc = "Cycle to a new map.")
    @CommandPermissions({"tgm.cycle"})
    public static void cycle(CommandContext cmd, CommandSender sender) {
        try {
            TGM.getMatchManager().cycleNextMatch();
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(e.getMessage());
        }
    }

    @Command(aliases = {"start"}, desc = "End the match.")
    @CommandPermissions({"tgm.start"})
    public static void start(CommandContext cmd, CommandSender sender) {
        MatchStatus matchStatus = TGM.getMatchManager().getMatch().getMatchStatus();
        if (matchStatus == MatchStatus.PRE) {
            TGM.getMatchManager().startMatch();
        } else {
            sender.sendMessage(ChatColor.RED + "The match cannot be started at this time.");
        }
    }

    @Command(aliases = {"end"}, desc = "Start the match.")
    @CommandPermissions({"tgm.end"})
    public static void end(CommandContext cmd, CommandSender sender) {
        MatchStatus matchStatus = TGM.getMatchManager().getMatch().getMatchStatus();
        if (matchStatus == MatchStatus.MID) {
            TGM.getMatchManager().endMatch();
        } else {
            sender.sendMessage(ChatColor.RED + "No match in progress.");
        }
    }

}
