package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.warzoneapi.models.RankUpdateRequest;
import network.warzone.warzoneapi.models.RankUpdateResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Jorge on 2/23/2018.
 */
public class RankCommands {

    @Command(aliases = {"rank", "ranks"}, desc = "Rank management command.", min = 1, usage = "(player|create|delete|set|addPerms)")
    @CommandPermissions({"tgm.command.rank"})
    public static void rank(CommandContext cmd, CommandSender sender) {
        if (cmd.getString(0).equalsIgnoreCase("player")) {
            ///rank player <player 1> <add|remove 2> <rank 3>
            if (cmd.argsLength() < 4) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " player <player> <add|remove> <rank>");
                return;
            }
            RankUpdateRequest.Action action = RankUpdateRequest.Action.valueOf(cmd.getString(2).toUpperCase());
            if (action == null) {
                sender.sendMessage(ChatColor.RED + "Unknown action: " + cmd.getString(2));
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " player <player> <add|remove> <rank>");
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                RankUpdateResponse response = TGM.get().getTeamClient().updateRank(cmd.getString(1), action, new RankUpdateRequest(cmd.getString(3)));
                if (response.isError()) {
                    sender.sendMessage(ChatColor.RED + response.getMessage());
                    return;
                } else {
                    Player target = Bukkit.getPlayer(cmd.getString(1));
                    sender.sendMessage(ChatColor.GRAY + "Added rank " + ChatColor.RESET + response.getRank().getName() + ChatColor.GRAY + " to " + ChatColor.RESET + (target != null ? target.getName() : cmd.getString(1)));
                    if (target != null) {
                        TGM.get().getPlayerManager().getPlayerContext(target).getUserProfile().addRank(response.getRank());
                    }
                }
            });
        }
    }

}
