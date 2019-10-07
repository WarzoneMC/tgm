package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.HoverEvent;
import network.warzone.tgm.TGM;
import network.warzone.tgm.util.Players;
import network.warzone.tgm.util.ServerUtil;
import network.warzone.tgm.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/*
These commands MUST NOT use ANY API FUNCTIONALITY. This class will
be enabled WHETHER THE API IS ENABLED OR NOT.
 */

public class MiscCommands {

    @Command(aliases = {"ping"}, desc = "Check player ping", max = 1, usage = "(name)")
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

    // TODO: Provide more essential info
    @Command(aliases = {"tgm"}, desc = "Get essential server info.")
    @CommandPermissions({"tgm.command.tgm"})
    public static void tgm(CommandContext commandContext, CommandSender sender) {
        String uptime = Strings.getFullAgo(TGM.get().getStartupTime());

        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "SERVER INFO");
        sender.sendMessage(String.format("%sUptime: %s%s", ChatColor.GRAY, ChatColor.WHITE, uptime));
        sender.sendMessage(String.format("%sLoaded worlds (%s%d%1$s):", ChatColor.GRAY, ChatColor.WHITE, Bukkit.getWorlds().size()));
        sender.sendMessage(String.format("%sMemory usage: (%s%s/%s%1$s):", ChatColor.GRAY, ChatColor.WHITE, ServerUtil.getFormattedUsedMemory(), ServerUtil.getFormattedTotalMemory()));
        Bukkit.getWorlds().forEach(w -> sender.sendMessage(ChatColor.GRAY + " - " + ChatColor.WHITE + w.getName()));

        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "TGM INFO");
        sender.sendMessage(String.format("%sMatch number: %s%s", ChatColor.GRAY, ChatColor.WHITE, TGM.get().getMatchManager().getMatchNumber()));
        sender.sendMessage(String.format("%sPlayer contexts: %s%s%s", ChatColor.GRAY, ChatColor.WHITE, TGM.get().getPlayerManager().getPlayers().size(),
                TGM.get().getPlayerManager().getPlayers().size() != Bukkit.getOnlinePlayers().size() ? ChatColor.RED + "" + ChatColor.BOLD + " !" : ""));
        sender.sendMessage(String.format("%sModules loaded: %s%s", ChatColor.GRAY, ChatColor.WHITE, TGM.get().getMatchManager().getMatch().getModules().size()));

    }

}
