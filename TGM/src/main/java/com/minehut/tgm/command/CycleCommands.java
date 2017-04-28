package com.minehut.tgm.command;

import com.minehut.tgm.TGM;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class CycleCommands {

    @Command(aliases = {"cycle"}, desc = "Cycle maps")
    @CommandPermissions({"tgm.cycle"})
    public static void cycle(CommandContext cmd, CommandSender sender) {
        try {
            TGM.getMatchManager().cycleNextMatch();
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(e.getMessage());
        }
    }


}
