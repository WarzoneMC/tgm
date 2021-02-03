package network.warzone.tgm.command;

import javax.annotation.Nullable;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;

import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.map.MapLibrary;
import network.warzone.tgm.map.source.GitRemoteMapSource;
import network.warzone.tgm.map.source.GitRemoteMapSource.RepoData;
import network.warzone.tgm.modules.kit.KitEditorModule;
import network.warzone.tgm.nickname.ProfileCache;
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

    @Command(aliases = {"remotes"}, desc = "Manage remote map sources", usage = "(name)")
    public static void remotes(CommandContext cmd, CommandSender sender) throws CommandPermissionsException {
        if (sender instanceof Player) {
            if (!sender.hasPermission("tgm.manage_remotes")) {
                throw new CommandPermissionsException();
            }
        }

        MapLibrary mapLibrary = TGM.get().getMatchManager().getMapLibrary();
        if (cmd.argsLength() == 0) {
            sender.sendMessage("Remotes: " + ChatColor.GREEN + mapLibrary.getRemotes().size());
            sender.sendMessage("");
            sender.sendMessage("For more information about specific remotes: " + ChatColor.YELLOW + "/remotes <update/view> <all/(remote name)>");
            sender.sendMessage(ChatColor.GREEN + "- update " + ChatColor.WHITE + "= Fetching maps from remote source");
            sender.sendMessage(ChatColor.GREEN + "- view " + ChatColor.WHITE + " = View metadata about remote source");
            sender.sendMessage("");
            return;
        }

        final String action = cmd.getString(0);
        final String remote = cmd.argsLength() == 1 ? "ALL" : cmd.getString(1);

        if ("UPDATE".equalsIgnoreCase(action)) {
            mapLibrary.updateRemote(remote, sender);
        } else {
            if (cmd.argsLength() == 1) {
                sender.sendMessage(ChatColor.RED + "Please specify a remote name");
                return;
            }
            sendRepoData(sender, remote);
        }
    }

    private static void sendRepoData(CommandSender sender, final String remote) {
        GitRemoteMapSource remoteMapSource = TGM.get().getMatchManager().getMapLibrary().getRemoteByName(remote);
        if (remoteMapSource == null) {
            sender.sendMessage(ChatColor.RED + "Remote name '" + remote + "' could not be recognized");
            return;
        }

        RepoData repoData = remoteMapSource.getRepoData();

        if (repoData == null) {
            sender.sendMessage(ChatColor.RED + "Could not get data about local repository");
        } else {
            sender.sendMessage(ChatColor.WHITE + "Local Repository Data for remote " + ChatColor.GREEN + repoData.NAME);
            sender.sendMessage("");
            sender.sendMessage(ChatColor.WHITE + "HEAD: " + ChatColor.GREEN + repoData.HEAD_ID);
            sender.sendMessage(ChatColor.WHITE + "HEAD Commit Message: " + ChatColor.GREEN + repoData.HEAD_MESSAGE);
            sender.sendMessage(ChatColor.WHITE + "Branch: " + ChatColor.GREEN + repoData.BRANCH);
        }
    }

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

        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "SERVER INFO");
        sender.sendMessage(String.format("%sUptime: %s%s", ChatColor.GRAY, ChatColor.WHITE, uptime));
        sender.sendMessage(String.format("%sMemory usage: (%s%s/%s%1$s):", ChatColor.GRAY, ChatColor.WHITE, ServerUtil.getFormattedUsedMemory(), ServerUtil.getFormattedTotalMemory()));
        sender.sendMessage(String.format("%sLoaded worlds (%s%d%1$s):", ChatColor.GRAY, ChatColor.WHITE, Bukkit.getWorlds().size()));
        Bukkit.getWorlds().forEach(w -> sender.sendMessage(ChatColor.GRAY + " - " + ChatColor.WHITE + w.getName()));

        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "TGM INFO");
        sender.sendMessage(String.format("%sMatch number: %s%s", ChatColor.GRAY, ChatColor.WHITE, TGM.get().getMatchManager().getMatchNumber()));
        sender.sendMessage(String.format("%sPlayer contexts: %s%s%s", ChatColor.GRAY, ChatColor.WHITE, TGM.get().getPlayerManager().getPlayers().size(),
                TGM.get().getPlayerManager().getPlayers().size() != Bukkit.getOnlinePlayers().size() ? ChatColor.RED + "" + ChatColor.BOLD + " !" : ""));
        sender.sendMessage(String.format("%sModules loaded: %s%s", ChatColor.GRAY, ChatColor.WHITE, TGM.get().getMatchManager().getMatch().getModules().size()));
        sender.sendMessage(String.format("%sCached profiles: %s%s", ChatColor.GRAY, ChatColor.WHITE, ProfileCache.getInstance().size()));
    }

    @Command(aliases = {"profilecache"}, desc = "Manage the profile cache.", usage = "<clear>", min = 1)
    @CommandPermissions({"tgm.command.profilecache"})
    public static void profileCache(CommandContext commandContext, CommandSender sender) {
        if ("clear".equalsIgnoreCase(commandContext.getString(0))) {
            ProfileCache.getInstance().clear();
            sender.sendMessage(ChatColor.YELLOW + "Cleared the profile cache.");
        }
    }

    @Command(aliases = {"kiteditor", "ke"}, desc = "Manages the kit layout editor", usage = "<on|off>", min = 1)
    @CommandPermissions({"tgm.kiteditor.manage"})
    public static void kitEditor(CommandContext commandContext, CommandSender sender) {
        KitEditorModule kitEditorModule = TGM.get().getModule(KitEditorModule.class);
        if ("on".equalsIgnoreCase(commandContext.getString(0))) {
            KitEditorModule.setEnabled(true);
            kitEditorModule.load();
            if (KitEditorModule.isKitEditable()) kitEditorModule.applyItem();
            sender.sendMessage(ChatColor.GREEN + "Enabled kit layout editing.");
        } else if ("off".equalsIgnoreCase(commandContext.getString(0))) {
            KitEditorModule.setEnabled(false);
            kitEditorModule.unload();
            sender.sendMessage(ChatColor.GREEN + "Disabled kit layout editing.");
        } else {
            sender.sendMessage(ChatColor.RED + "/kiteditor <on|off>");
        }
    }

    @Command(aliases = {"rules"}, desc = "View the server rules.")
    public static void rules(CommandContext commandContext, CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Please read and abide by our rules which can be found here: " + TGM.get().getConfig().getString("server.rules"));
    }
}
