package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.vote.VoteSkipModule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkipCommands {
    @Command(aliases = {"skip"}, desc = "Vote skip a map")
    public static void skip(CommandContext cmd, CommandSender sender) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return;
        }
        Player player = (Player) sender;
        if(TGM.get().getMatchManager().getMatch().getMatchStatus() != MatchStatus.MID) {
            player.sendMessage(ChatColor.RED + "There is no match in progress");
            return;
        }
        VoteSkipModule voteSkipModule = TGM.get().getModule(VoteSkipModule.class);
        boolean success = voteSkipModule.addVote(player);
        player.sendMessage(success ? ChatColor.GREEN + "You have voted to skip the current match." : ChatColor.RED + "You have already voted!");
        int stillNeeds = voteSkipModule.stillNeeds();
        if(stillNeeds > 0) player.sendMessage(ChatColor.GOLD.toString() + stillNeeds + ChatColor.YELLOW + " votes are needed to skip the current map.");
    }
}
