package com.minehut.tgm.command;

import com.minehut.tgm.TGM;
import com.minehut.tgm.map.MapContainer;
import com.minehut.tgm.match.MatchStatus;
import com.minehut.tgm.modules.ChatModule;
import com.minehut.tgm.modules.countdown.Countdown;
import com.minehut.tgm.modules.countdown.CycleCountdown;
import com.minehut.tgm.modules.countdown.StartCountdown;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.modules.team.TeamUpdateEvent;
import com.minehut.tgm.user.PlayerContext;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandNumberFormatException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CycleCommands {

    @Command(aliases = {"maps"}, desc = "View the maps that are on team.gg, although not necessarily in the rotation.")
    @CommandPermissions({"tgm.maps"})
    public static void maps(CommandContext cmd, CommandSender sender) {
        List<String> maps = new ArrayList<>();
        int i = 1;
        for (MapContainer mapContainer : TGM.get().getMatchManager().getMapLibrary().getMaps()) {
            if (mapContainer.equals(TGM.get().getMatchManager().getMatch().getMapContainer())) {
                maps.add(ChatColor.GREEN + String.valueOf(i) + ". " + mapContainer.getMapInfo().getName());
            } else {
                maps.add(ChatColor.GRAY + String.valueOf(i) + ". " + mapContainer.getMapInfo().getName());
            }
            i++;
        }

        sender.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Maps: \n" + StringUtils.join(maps, "\n"));
    }

    @Command(aliases = {"rot", "rotation", "rotations"}, desc = "View the maps that are in the rotation.")
    @CommandPermissions({"tgm.rotation"})
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

        sender.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Current Rotation: \n" + StringUtils.join(maps, "\n"));
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
            } else {
                sender.sendMessage(ChatColor.RED + "/team alias (team) (name)");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "/team alias (team) (name)");
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


    public static void attemptJoinTeam(Player player, MatchTeam matchTeam, boolean autoJoin) {
        if (matchTeam.getMembers().size() >= matchTeam.getMax()) {
            player.sendMessage(ChatColor.RED + "Team is full! Wait for a spot to open up.");
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
