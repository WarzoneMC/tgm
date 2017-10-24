package network.warzone.tgm.command;

import network.warzone.tgm.TGM;
import network.warzone.tgm.gametype.GameType;
import network.warzone.tgm.map.MapContainer;
import network.warzone.tgm.match.MatchManager;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.ChatModule;
import network.warzone.tgm.modules.countdown.Countdown;
import network.warzone.tgm.modules.countdown.CycleCountdown;
import network.warzone.tgm.modules.countdown.StartCountdown;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.team.TeamUpdateEvent;
import network.warzone.tgm.user.PlayerContext;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandNumberFormatException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CycleCommands {

    @Command(aliases = {"maps"}, desc = "View the maps that are on Warzone, although not necessarily in the rotation.")
    public static void maps(CommandContext cmd, CommandSender sender) {
        List<String> maps = new ArrayList<>();
        int i = 1;
        for (MapContainer mapContainer : TGM.get().getMatchManager().getMapLibrary().getMaps()) {
            maps.add(ChatColor.GRAY + String.valueOf(i) + ". " + mapContainer.getMapInfo().getName());
            i++;
        }

        sender.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Maps: \n" + StringUtils.join(maps, "\n"));
    }

    @Command(aliases = {"rot", "rotation", "rotations"}, desc = "View the maps that are in the rotation.")
    public static void rotation(CommandContext cmd, CommandSender sender) {
        List<String> maps = new ArrayList<>();
        int i = 1;
        for (MapContainer mapContainer : TGM.get().getMatchManager().getMapRotation().getMaps()) {
            if (mapContainer.equals(TGM.get().getMatchManager().getMatch().getMapContainer())) {
                maps.add(ChatColor.GREEN + String.valueOf(i) + ". " + mapContainer.getMapInfo().getName());
            } else {
                maps.add(ChatColor.GRAY + String.valueOf(i) + ". " + mapContainer.getMapInfo().getName());
            }
            i++;
        }

        sender.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Rotation: \n" + StringUtils.join(maps, "\n"));
    }


    @Command(aliases = {"cycle"}, desc = "Cycle to a new map.")
    @CommandPermissions({"tgm.cycle"})
    public static void cycle(CommandContext cmd, CommandSender sender) {
        MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();
        if (matchStatus != MatchStatus.MID) {
            int time = CycleCountdown.START_TIME;
            if (cmd.argsLength() > 0) {
                try {
                    time = cmd.getInteger(0);
                } catch (CommandNumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Unknown time \"" + cmd.getString(0) + "\"");
                }
            }
            TGM.get().getModule(CycleCountdown.class).start(time);
        } else {
            sender.sendMessage(ChatColor.RED + "A match is currently in progress.");
        }
    }

    @Command(aliases = {"start"}, desc = "Start the match.")
    @CommandPermissions({"tgm.start"})
    public static void start(CommandContext cmd, CommandSender sender) {
        MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();
        if (matchStatus == MatchStatus.PRE) {
            int time = StartCountdown.START_TIME;
            if (cmd.argsLength() > 0) {
                try {
                    time = cmd.getInteger(0);
                } catch (CommandNumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Unknown time \"" + cmd.getString(0) + "\"");
                }
            }
            TGM.get().getModule(StartCountdown.class).start(time);
        } else {
            sender.sendMessage(ChatColor.RED + "The match cannot be started at this time.");
        }
    }

    @Command(aliases = {"end"}, desc = "End the match.")
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

    @Command(aliases = {"cancel"}, desc = "Cancel all countdowns.")
    @CommandPermissions({"tgm.cancel"})
    public static void cancel(CommandContext cmd, CommandSender sender) {
        for (Countdown countdown : TGM.get().getModules(Countdown.class)) {
            countdown.cancel();
        }
        sender.sendMessage(ChatColor.GREEN + "Countdowns cancelled.");
    }

    @Command(aliases = {"setnext", "sn"}, desc = "Set the next map.")
    @CommandPermissions({"tgm.setnext"})
    public static void setNext(CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() > 0) {
            MapContainer found = null;
            for (MapContainer mapContainer : TGM.get().getMatchManager().getMapLibrary().getMaps()) {
                if (mapContainer.getMapInfo().getName().equalsIgnoreCase(cmd.getJoinedStrings(0))) {
                    found = mapContainer;
                }
            }
            for (MapContainer mapContainer : TGM.get().getMatchManager().getMapLibrary().getMaps()) {
                if (mapContainer.getMapInfo().getName().toLowerCase().startsWith(cmd.getJoinedStrings(0).toLowerCase())) {
                    found = mapContainer;
                }
            }

            if (found == null) {
                sender.sendMessage(ChatColor.RED + "Map not found \"" + cmd.getJoinedStrings(0) + "\"");
                return;
            }

            TGM.get().getMatchManager().setForcedNextMap(found);
            sender.sendMessage(ChatColor.GREEN + "Set the next map to " + ChatColor.YELLOW + found.getMapInfo().getName() + ChatColor.GRAY + " (" + found.getMapInfo().getVersion() + ")");
        } else {
            sender.sendMessage(ChatColor.RED + "/sn <map_name>");
        }
    }

    @Command(aliases = {"join", "play"}, desc = "Join a team.")
    public static void join(CommandContext cmd, CommandSender sender) {
        TeamManagerModule teamManager = TGM.get().getModule(TeamManagerModule.class);
        MatchManager matchManager = TGM.get().getMatchManager();
        if (cmd.argsLength() == 0) {
            if (matchManager.getMatch().getMapContainer().getMapInfo().getGametype().equals(GameType.Blitz)) {
                if (!matchManager.getMatch().getMatchStatus().equals(MatchStatus.PRE)) {
                    sender.sendMessage(ChatColor.RED + "You can't pick a team after the match starts in this gamemode.");
                    return;
                }
            }
            if (teamManager.getTeam((Player) sender).isSpectator() || matchManager.getMatch().getMatchStatus().equals(MatchStatus.PRE)) {
                if (matchManager.getMatch().getMapContainer().getMapInfo().getGametype().equals(GameType.Infected)) {
                    if (matchManager.getMatch().getMatchStatus().equals(MatchStatus.MID) || matchManager.getMatch().getMatchStatus().equals(MatchStatus.POST)) {
                        MatchTeam team = teamManager.getTeamById("infected");
                        attemptJoinTeam((Player) sender, team, true);
                        return;
                    }

                    MatchTeam team = teamManager.getTeamById("humans");
                    attemptJoinTeam((Player) sender, team, true);
                    return;
                }
                MatchTeam matchTeam = teamManager.getSmallestTeam();
                attemptJoinTeam((Player) sender, matchTeam, true);
            } else {
                sender.sendMessage(ChatColor.RED + "You have already chosen a team.");
            }
        } else {
            MatchTeam matchTeam = teamManager.getTeamFromInput(cmd.getJoinedStrings(0));

            if (matchTeam == null) {
                sender.sendMessage(ChatColor.RED + "Unable to find team \"" + cmd.getJoinedStrings(0) + "\"");
                return;
            }

            if (matchManager.getMatch().getMapContainer().getMapInfo().getGametype().equals(GameType.Infected)) {
                if (matchManager.getMatch().getMatchStatus().equals(MatchStatus.POST)) {
                    if (!matchTeam.isSpectator()) {
                        sender.sendMessage(ChatColor.RED + "The game has already ended.");
                        return;
                    } else {
                        attemptJoinTeam((Player) sender, matchTeam, false);
                        return;
                    }
                } else if (matchManager.getMatch().getMatchStatus().equals(MatchStatus.MID)) {
                    if (!matchTeam.isSpectator()) {
                        sender.sendMessage(ChatColor.RED + "You can't pick a team after the match starts in this gamemode.");
                        return;
                    } else {
                        attemptJoinTeam((Player) sender, matchTeam, false);
                        return;
                    }
                }
            } else if (matchManager.getMatch().getMapContainer().getMapInfo().getGametype().equals(GameType.Blitz)) {
                if (!matchManager.getMatch().getMatchStatus().equals(MatchStatus.PRE)) {
                    sender.sendMessage(ChatColor.RED + "You can't pick a team after the match starts in this gamemode.");
                    return;
                }
            }

            attemptJoinTeam((Player) sender, matchTeam, false);
        }
    }

    @Command(aliases = {"team"}, desc = "Manage teams.")
    @CommandPermissions({"tgm.team"})
    public static void team(CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() > 0) {
            if (cmd.getString(0).equalsIgnoreCase("alias")) {
                if (cmd.argsLength() == 3) {
                    MatchTeam matchTeam = TGM.get().getModule(TeamManagerModule.class).getTeamFromInput(cmd.getString(1));
                    if (matchTeam == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown team \"" + cmd.getString(1) + "\"");
                        return;
                    }
                    matchTeam.setAlias(cmd.getString(2));
                    Bukkit.getPluginManager().callEvent(new TeamUpdateEvent(matchTeam));
                } else {
                    sender.sendMessage(ChatColor.RED + "/team alias (team) (name)");
                }
            } else if (cmd.getString(0).equalsIgnoreCase("force")) {
                if (cmd.argsLength() == 3) {
                    MatchTeam matchTeam = TGM.get().getModule(TeamManagerModule.class).getTeamFromInput(cmd.getString(2));
                    if (matchTeam == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown team \"" + cmd.getString(2) + "\"");
                        return;
                    }
                    Player player = Bukkit.getPlayer(cmd.getString(1));
                    if (player == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown player \"" + cmd.getString(1) + "\"");
                        return;
                    }
                    attemptJoinTeam(player, matchTeam, true, true);
                    sender.sendMessage(ChatColor.GREEN + "Forced " + player.getName() + " into " + matchTeam.getColor() + matchTeam.getAlias());
                } else {
                    sender.sendMessage(ChatColor.RED + "/team force (player) (team)");
                }
            } else if (cmd.getString(0).equalsIgnoreCase("size")) {
                if (cmd.argsLength() == 4) {
                    MatchTeam matchTeam = TGM.get().getModule(TeamManagerModule.class).getTeamFromInput(cmd.getString(1));
                    if (matchTeam == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown team \"" + cmd.getString(1) + "\"");
                        return;
                    }
                    int min = 0;
                    int max = 0;
                    try {
                        min = cmd.getInteger(2);
                        max = cmd.getInteger(3);
                    } catch (CommandNumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
                        return;
                    }
                    matchTeam.setMin(min);
                    matchTeam.setMax(max);
                    Bukkit.getPluginManager().callEvent(new TeamUpdateEvent(matchTeam));
                    sender.sendMessage(ChatColor.GREEN + "Set " + matchTeam.getColor() + matchTeam.getAlias() + ChatColor.GREEN + " size limits to " + min + "-" + max);
                } else {
                    sender.sendMessage(ChatColor.RED + "/team size (team) (min) (max)");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "/team alias|force|size");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "/team alias|force|size");
        }
    }

    @Command(aliases = {"loadmaps"}, desc = "Load maps.")
    @CommandPermissions({"tgm.loadmaps"})
    public static void loadmaps(CommandContext cmd, CommandSender sender) {
        TGM.get().getMatchManager().getMapLibrary().refreshMaps();
        TGM.get().getMatchManager().getMapRotation().refresh();
        sender.sendMessage(ChatColor.GREEN + "Refreshed map library and rotation.");
    }

    @Command(aliases = {"t"}, desc = "Send a message to your team.", usage = "(message)", min = 1)
    public static void t(CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() > 0) {
            PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext((Player) sender);
            TGM.get().getModule(ChatModule.class).sendTeamChat(playerContext, cmd.getJoinedStrings(0));
        }
        
        @Command(aliases = "next", desc = "View the next map in the rotation")
public static void next(CommandContext cmd, CommandSender sender) {
    sender.sendMessage(ChatColor.YELLOW + "Next Map: " + ChatColor.GRAY + TGM.get().getMatchManager().getMapRotation().getNext().getMapInfo().getName());
}

    @Command(aliases = "next", desc = "View the next map in the rotation")
    public static void next(CommandContext cmd, CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Next Map: " + ChatColor.GRAY + TGM.get().getMatchManager().getMapRotation().getNext().getMapInfo().getName());
    }
    
    @Command(aliases = "map", desc = "View the map info for the current map")
    public static void map(CommandContext cmd, CommandSender sender) {
        MapInfo info = TGM.get().getMatchManager().getMapRotation().getCurrent().getMapInfo();
        sender.sendMessage(ChatColor.GRAY + "Currently playing " + ChatColor.YELLOW + info.getGametype + ChatColor.GRAY + " on map " + ChatColor.YELLOW + info.getName + ChatColor.GRAY + "by " + ChatColor.YELLOW + StringUtils.join(info.getAuthors(), ", ")());
    }

    @Command(aliases = {"config"}, desc = "Edit the configuration", usage = "(stats)", min = 1)
    @CommandPermissions({"tgm.config"})
    public static void config(CommandContext cmd, CommandSender sender) {
        if (cmd.getString(0).equalsIgnoreCase("stats")) {
            if (cmd.argsLength() != 2) {
                sender.sendMessage(ChatColor.WHITE + "Stat uploading is set to \"" + TGM.get().getConfig().getBoolean("api.stats.enabled") + "\"");
                return;
            }
            if (cmd.getString(1).equalsIgnoreCase("off")) {
                TGM.get().getConfig().set("api.stats.enabled", false);
                TGM.get().saveConfig();

                sender.sendMessage(ChatColor.GREEN + "Disabled stat uploading.");
            } else if (cmd.getString(1).equalsIgnoreCase("on")) {
                TGM.get().getConfig().set("api.stats.enabled", true);
                TGM.get().saveConfig();

                sender.sendMessage(ChatColor.GREEN + "Enabled stat uploading.");
            } else {
                sender.sendMessage(ChatColor.RED + "Unknown value \"" + cmd.getString(0) + "\". Please specify [on/off]");
            }
        }
    }

    @Command(aliases = {"stats", "stat"}, desc = "View your stats.")
    public static void stats(final CommandContext cmd, CommandSender sender) {
        Player player = (Player) sender;

        if (cmd.argsLength() == 0) {
            viewStats(player, player.getName());
        } else {
            Player target = Bukkit.getPlayer(cmd.getString(0));
            viewStats(player, cmd.getString(0));
        }
    }

    public static void viewStats(Player player, String target) {
        Player targetPlayer = Bukkit.getServer().getPlayer(target);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Unable to find online player " + ChatColor.YELLOW + target);
            return;
        }

        PlayerContext targetUser = TGM.get().getPlayerManager().getPlayerContext(targetPlayer);
        player.sendMessage(ChatColor.BLUE + "-------------------------------");
        player.sendMessage(ChatColor.DARK_AQUA + "   Viewing stats for " +  ChatColor.AQUA + targetPlayer.getName());
        player.sendMessage("   Level: " + ChatColor.GREEN + targetUser.getLevelString().replace("[", "").replace("]", ""));
        player.sendMessage("   Kills: " + ChatColor.GREEN + targetUser.getUserProfile().getKills());
        player.sendMessage("   Deaths: " + ChatColor.GREEN + targetUser.getUserProfile().getDeaths());
        player.sendMessage("   K/D: " + ChatColor.GREEN + targetUser.getUserProfile().getKDR());
        player.sendMessage("   Wins: " + ChatColor.GREEN + targetUser.getUserProfile().getWins());
        player.sendMessage("   Losses: " + ChatColor.GREEN + targetUser.getUserProfile().getLosses());
        player.sendMessage("   W/L: " + ChatColor.GREEN + targetUser.getUserProfile().getWLR());
        player.sendMessage(ChatColor.BLUE + "-------------------------------");
    }


    public static void attemptJoinTeam(Player player, MatchTeam matchTeam, boolean autoJoin) {
        attemptJoinTeam(player, matchTeam, autoJoin, false);
    }

    public static void attemptJoinTeam(Player player, MatchTeam matchTeam, boolean autoJoin, boolean ignoreFull) {
        if (!ignoreFull && autoJoin && !player.hasPermission("tgm.pickteam") && !TGM.get().getModule(TeamManagerModule.class).getTeam(player).isSpectator()) {
            player.sendMessage(ChatColor.RED + "You are already in a team.");
            return;
        }
        if (matchTeam.getMembers().size() >= matchTeam.getMax() && !ignoreFull) {
            player.sendMessage(ChatColor.RED + "Team is full! Wait for a spot to open up.");
            return;
        }

        if (!autoJoin) {
            if (!player.hasPermission("tgm.pickteam")) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Only premium users can pick their team! Purchase a rank at http://warzone.store/");
                return;
            }
        }

        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(player);
        TGM.get().getModule(TeamManagerModule.class).joinTeam(playerContext, matchTeam);
    }

}
