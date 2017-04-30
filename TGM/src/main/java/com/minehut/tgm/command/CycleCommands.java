package com.minehut.tgm.command;

import com.minehut.tgm.TGM;
import com.minehut.tgm.match.MatchStatus;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class CycleCommands {

    @Command(aliases = {"cycle"}, desc = "Cycle to a new map.")
    @CommandPermissions({"tgm.cycle"})
    public static void cycle(CommandContext cmd, CommandSender sender) {
        try {
            TGM.get().getMatchManager().cycleNextMatch();
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(e.getMessage());
        }
    }

    @Command(aliases = {"start"}, desc = "End the match.")
    @CommandPermissions({"tgm.start"})
    public static void start(CommandContext cmd, CommandSender sender) {
        MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();
        if (matchStatus == MatchStatus.PRE) {
            TGM.get().getMatchManager().startMatch();
        } else {
            sender.sendMessage(ChatColor.RED + "The match cannot be started at this time.");
        }
    }

    @Command(aliases = {"end"}, desc = "Start the match.")
    @CommandPermissions({"tgm.end"})
    public static void end(CommandContext cmd, CommandSender sender) {
        MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();
        if (matchStatus == MatchStatus.MID) {
            if (cmd.argsLength() > 0) {
                MatchTeam matchTeam = TGM.get().getModule(TeamManagerModule.class).getTeamFromInput(cmd.getJoinedStrings(0));
                if (matchTeam == null) {
                    sender.sendMessage(ChatColor.RED + "Unable to find team \"" + cmd.getJoinedStrings(0) + "\"");
                    return;
                }
                TGM.get().getMatchManager().endMatch(matchTeam);
            } else {
                TGM.get().getMatchManager().endMatch(TGM.get().getModule(TeamManagerModule.class).getTeams().get(1));
            }
        } else {
            sender.sendMessage(ChatColor.RED + "No match in progress.");
        }
    }

    @Command(aliases = {"join"}, desc = "Join a team.")
    public static void join(CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() == 0) {
            MatchTeam matchTeam = TGM.get().getModule(TeamManagerModule.class).getSmallestTeam();
            attemptJoinTeam((Player) sender, matchTeam, true);
        } else {
            MatchTeam matchTeam = TGM.get().getModule(TeamManagerModule.class).getTeamFromInput(cmd.getJoinedStrings(0));
            if (matchTeam == null) {
                sender.sendMessage(ChatColor.RED + "Unable to find team \"" + cmd.getJoinedStrings(0) + "\"");
                return;
            }

            attemptJoinTeam((Player) sender, matchTeam, false);
        }
    }

    public static void attemptJoinTeam(Player player, MatchTeam matchTeam, boolean autoJoin) {
        if (matchTeam.getMembers().size() >= matchTeam.getMax()) {
            player.sendMessage(ChatColor.RED + "Team full! Wait for a spot to open up.");
            return;
        }

        if (!autoJoin) {
            if (!player.hasPermission("tgm.pickteam")) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Only premium users can choose their team. Use " + ChatColor.WHITE + "Auto Join " + ChatColor.LIGHT_PURPLE + "instead.");
                return;
            }
        }

        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(player);
        TGM.get().getModule(TeamManagerModule.class).joinTeam(playerContext, matchTeam);
    }

}
