package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import network.warzone.tgm.TGM;
import network.warzone.warzoneapi.models.IssuePunishmentRequest;
import network.warzone.warzoneapi.models.IssuePunishmentResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PunishCommands {

    @Command(aliases = "ban", desc = "Ban a rulebreaker", min = 2, usage = "(name) (length) (reason...)")
    public static void ban(CommandContext cmd, Player sender) {
        String name = cmd.getString(0);

        String lengthString = cmd.getString(1);
        long length = 1;

        String reason = cmd.argsLength() > 2 ? cmd.getRemainingString(2) : "Inappropriate Behavior";

        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
            IssuePunishmentResponse response = TGM.get().getTeamClient().issuePunishment(
                    new IssuePunishmentRequest(name, sender.getUniqueId(), "BAN", length, reason));
            if (response.isNotFound()) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
            } else {
                Bukkit.broadcastMessage(ChatColor.GRAY + sender.getName() + " banned " + ChatColor.RED + response.getName() + ChatColor.GRAY +
                    " for " + ChatColor.RED + "'" + reason + "'");
            }
        });
    }

}
